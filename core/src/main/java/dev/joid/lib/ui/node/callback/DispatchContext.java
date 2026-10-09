package dev.joid.lib.ui.node.callback;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class DispatchContext {

	private final List<Node> path;

	private boolean cancelled;

	protected DispatchContext(final List<Node> path, final boolean cancelled) {
		this.path      = path;
		this.cancelled = cancelled;
	}

	public static @NonNull DispatchContext create() {
		return new DispatchContext(Collections.emptyList(), false);
	}

	public static @NonNull DispatchContext create(final boolean cancelled) {
		return new DispatchContext(Collections.emptyList(), cancelled);
	}

	public static @NonNull DispatchContext create(final @NonNull List<@NonNull Node> path) {
		return new DispatchContext(path, false);
	}

	public @NonNull DispatchContext execute(final @NonNull Runnable runnable) {
		if (!this.cancelled) {
			runnable.run();
		}
		return this;
	}

	public @NonNull DispatchContext cancel() {
		this.cancelled = true;
		return this;
	}

	public @NonNull DispatchContext cancel(final @NonNull Runnable runnable) {
		if (this.cancelled) {
			return this;
		}

		runnable.run();
		this.cancel();
		return this;
	}

	public @NonNull DispatchContext cancelIf(final @NonNull Supplier<@NonNull Boolean> supplier) {
		if (this.cancelled) {
			return this;
		}

		if (supplier.get()) {
			this.cancel();
		}

		return this;
	}

	public @NonNull DispatchContext reset() {
		this.cancelled = false;
		return this;
	}

	public boolean isOnPath(final @NonNull Node node) {
		return this.path.contains(node);
	}

	public Node getTarget() {
		return this.path.isEmpty() ? null : this.path.get(0);
	}

}