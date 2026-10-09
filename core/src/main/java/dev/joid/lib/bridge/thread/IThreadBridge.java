package dev.joid.lib.bridge.thread;

import dev.joid.lib.bridge.IBridge;
import lombok.NonNull;

public interface IThreadBridge extends IBridge {

	public boolean isRenderThread();

	public void execute(final @NonNull Runnable runnable);

}