package dev.joid.lib.bridge.thread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.joid.internal.JOIDLog;
import lombok.NonNull;

public final class FrameThreadBridge implements IThreadBridge {

	private final Thread          thread;
	private final Queue<Runnable> taskQueue;

	public FrameThreadBridge() {
		this.thread    = Thread.currentThread();
		this.taskQueue = new ConcurrentLinkedQueue<>();
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
		if (!this.isRenderThread()) {
			JOIDLog.warnOnce("FrameThreadBridge.thread", "The FrameThreadBridge was created on the thread " + this.thread.getName() + " but its frames run on the thread " + Thread.currentThread().getName() + ": create it on the render thread, or JOID takes the render thread for another thread");
		}

		Runnable task;
		while ((task = this.taskQueue.poll()) != null) {
			try {
				task.run();
			} catch (final RuntimeException exception) {
				final StackTraceElement[] trace = exception.getStackTrace();
				JOIDLog.warnOnce("FrameThreadBridge.task." + exception.getClass().getName() + "." + (trace.length > 0 ? trace[0] : task.getClass().getName()), "A task sent to the render thread from another thread failed (" + exception + "), the next tasks still run: catch the exception inside the task to handle it", exception);
			}
		}
	}

}