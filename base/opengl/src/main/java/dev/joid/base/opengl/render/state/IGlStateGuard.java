package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.IGlBinding;
import lombok.NonNull;

public interface IGlStateGuard {

	public void exit();
	public void enter();
	public void suspend(final @NonNull Runnable draw);

	public @NonNull IGlBinding getBinding();

}