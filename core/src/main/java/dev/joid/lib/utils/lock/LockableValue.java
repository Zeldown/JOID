package dev.joid.lib.utils.lock;

import java.util.function.UnaryOperator;

import dev.joid.internal.JOIDLog;

import lombok.Getter;
import lombok.NonNull;

public abstract class LockableValue<T extends LockableValue<T>> {

	@Getter
	private boolean locked;

	public abstract @NonNull T copy();

	public final @NonNull T lock() {
		this.locked = true;
		return (T) this;
	}

	protected final @NonNull T mutate(final @NonNull String copy, final @NonNull Runnable mutation) {
		if (this.locked) {
			throw new IllegalStateException(this.describeLocked() + ", use " + copy + "(...) to get a modified copy");
		}

		mutation.run();
		return (T) this;
	}

	protected final @NonNull T derive(final @NonNull UnaryOperator<T> mutation) {
		return mutation.apply(this.copy());
	}

	protected @NonNull String describeLocked() {
		return "This " + JOIDLog.nameOf(this.getClass()) + " is locked";
	}

}