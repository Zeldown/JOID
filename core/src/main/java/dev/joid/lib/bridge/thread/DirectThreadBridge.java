package dev.joid.lib.bridge.thread;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectThreadBridge implements IThreadBridge {

	public static @NonNull DirectThreadBridge create() {
		return new DirectThreadBridge();
	}

	@Override
	public boolean isRenderThread() {
		return true;
	}

	@Override
	public void execute(final @NonNull Runnable runnable) {
		runnable.run();
	}

}