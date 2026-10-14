package dev.joid.lib.bridge.thread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.joid.internal.JOIDLog;
import lombok.NonNull;

public final class FrameThreadBridge implements IThreadBridge {

	private final Thread          thread;
	private final Queue<Runnable> taskQueue;

	private FrameThreadBridge(final Thread thread) {
		this.thread    = thread;
		this.taskQueue = new ConcurrentLinkedQueue<>();
	}

	public static @NonNull FrameThreadBridge create() {
		return new FrameThreadBridge(Thread.currentThread());
	}

	@Override
	public boolean isRenderThread() {
		return Thread.currentThread() == this.thread;
	}

	@Override
	public void execute(final @NonNull Runnable runnable) {
		if (this.isRenderThread()) {
			runnable.run();
		} else {
			this.taskQueue.add(runnable);
		}
	}

	public void runTasks() {
		Runnable task;
		while ((task = this.taskQueue.poll()) != null) {
			try {
				task.run();
			} catch (final RuntimeException exception) {
				JOIDLog.warn("A task sent to the render thread from another thread failed (" + exception + "), the next tasks still run: catch the exception inside the task to handle it", exception);
			}
		}
	}

}