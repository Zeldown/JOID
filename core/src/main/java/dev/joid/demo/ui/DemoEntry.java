package dev.joid.demo.ui;

import java.util.function.Supplier;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.UI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoEntry {

	private final String   label;
	private final Runnable action;

	private String            hover;
	private Supplier<Boolean> state;

	public static @NonNull DemoEntry create(final @NonNull Class<? extends UI> clazz) {
		return new DemoEntry(clazz.getSimpleName(), () -> {
			try {
				final UI ui = clazz.newInstance();
				ui.setTransition(new DemoPushTransition());
				JOID.open(ui, false);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}).hover(clazz.getName());
	}

	public static @NonNull DemoEntry create(final @NonNull String label, final @NonNull Runnable action) {
		return new DemoEntry(label, action);
	}

	public @NonNull DemoEntry hover(final String hover) {
		this.hover = hover;
		return this;
	}

	public @NonNull DemoEntry state(final Supplier<Boolean> state) {
		this.state = state;
		return this;
	}

	public boolean isActive() {
		return this.state != null && Boolean.TRUE.equals(this.state.get());
	}

	public @NonNull String getText() {
		if (this.state == null) {
			return this.label;
		}

		return this.label + (this.isActive() ? ": on" : ": off");
	}

}