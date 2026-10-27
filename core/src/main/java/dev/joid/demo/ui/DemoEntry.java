package dev.joid.demo.ui;

import java.util.function.BooleanSupplier;

import dev.joid.demo.ui.core.transition.DemoPushTransition;
import dev.joid.lib.JOID;
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

	private String          hover;
	private BooleanSupplier state;

	public static @NonNull DemoEntry create(final @NonNull Class<? extends UI> clazz) {
		return new DemoEntry(clazz.getSimpleName(), () -> {
			try {
				final UI ui = clazz.newInstance();
				ui.transition(DemoPushTransition.create());
				JOID.open(ui);
			} catch (final ReflectiveOperationException exception) {
				throw new IllegalStateException("The demo " + clazz.getSimpleName() + " cannot be created (" + exception + "), give its UI a public constructor without argument", exception);
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

	public @NonNull DemoEntry state(final BooleanSupplier state) {
		this.state = state;
		return this;
	}

	public boolean isActive() {
		return this.state != null && this.state.getAsBoolean();
	}

	public @NonNull String getText() {
		if (this.state == null) {
			return this.label;
		}

		return this.label + (this.isActive() ? ": on" : ": off");
	}

}