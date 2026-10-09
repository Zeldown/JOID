package dev.joid.lib.ui.node.callback;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class DispatchContext {

	private boolean cancelled;

	protected DispatchContext(final boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static @NonNull DispatchContext create() {
		return new DispatchContext(false);
	}

	public static @NonNull DispatchContext create(final boolean cancelled) {
		return new DispatchContext(cancelled);
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

}