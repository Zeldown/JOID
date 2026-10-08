package dev.joid.base.opengl.render.host;

import dev.joid.base.opengl.binding.IGlBinding;
import lombok.NonNull;

public interface IGlHostGuard {

	public void exit();
	public void enter();
	public void host(final @NonNull Runnable host);

	public @NonNull IGlBinding getBinding();

}