package dev.joid.lib.bridge.thread;

import lombok.NonNull;

public final class DirectThreadBridge implements IThreadBridge {

	@Override
	public boolean isRenderThread() {
		return true;
	}

	@Override
	public void execute(final @NonNull Runnable runnable) {
		runnable.run();
	}

}