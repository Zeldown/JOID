package dev.joid.base.opengl.render.state;

import java.util.function.Supplier;

import dev.joid.base.opengl.binding.IGlBinding;
import lombok.NonNull;

public interface IGlStateGuard {

	public void exit();
	public void enter();
	public void suspend(final @NonNull Runnable draw);

	public default void run(final @NonNull Runnable action) {
		this.enter();
		try {
			action.run();
		} finally {
			this.exit();
		}
	}

	public default <T> T call(final @NonNull Supplier<T> action) {
		this.enter();
		try {
			return action.get();
		} finally {
			this.exit();
		}
	}

	public @NonNull IGlBinding getBinding();

}