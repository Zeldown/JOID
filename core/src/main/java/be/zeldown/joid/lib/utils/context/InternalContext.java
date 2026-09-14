package be.zeldown.joid.lib.utils.context;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class InternalContext {

	private boolean cancelled;

	protected InternalContext(final boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static @NonNull InternalContext create() {
		return new InternalContext(false);
	}

	public static @NonNull InternalContext create(final boolean cancelled) {
		return new InternalContext(cancelled);
	}

	public @NonNull InternalContext execute(final @NonNull Runnable runnable) {
		if (!this.cancelled) {
			runnable.run();
		}
		return this;
	}

	public @NonNull InternalContext cancel(final @NonNull Runnable runnable) {
		if (this.cancelled) {
			return this;
		}

		runnable.run();
		this.cancel();
		return this;
	}

	public @NonNull InternalContext cancel(final @NonNull Supplier<@NonNull Boolean> supplier) {
		if (this.cancelled) {
			return this;
		}

		if (supplier.get()) {
			this.cancel();
		}

		return this;
	}

	public @NonNull InternalContext cancel() {
		this.cancelled = true;
		return this;
	}

	public @NonNull InternalContext reset() {
		this.cancelled = false;
		return this;
	}

}