package dev.joid.lib.ui.node.property.watch;

import java.util.function.BiConsumer;

import dev.joid.lib.signal.ISignal;
import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WatchProperty {

	public static final WatchProperty BODY           = new WatchProperty((node, signal) -> {
		if (node.getBodyConsumer() != null) {
			node.getBodyConsumer().accept(node);
		}
	});
	public static final WatchProperty CLEAR_CHILDREN = new WatchProperty((node, signal) -> node.clearChildren());

	private final @NonNull BiConsumer<@NonNull Node, @NonNull ISignal<?>> action;

	public static @NonNull WatchProperty custom(final @NonNull BiConsumer<@NonNull Node, @NonNull ISignal<?>> action) {
		return new WatchProperty(action);
	}

	public void apply(final @NonNull Node node, final @NonNull ISignal<?> signal) {
		this.action.accept(node, signal);
	}

}