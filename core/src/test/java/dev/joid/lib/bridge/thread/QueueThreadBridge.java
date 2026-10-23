package dev.joid.lib.bridge.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.NonNull;

public final class QueueThreadBridge implements IThreadBridge {

	private final List<Runnable> tasks  = new ArrayList<>();
	private final CountDownLatch posted = new CountDownLatch(1);

	@Getter
	private boolean renderThread;

	@Override
	public void execute(final @NonNull Runnable runnable) {
		this.tasks.add(runnable);
		this.posted.countDown();
	}

	public @NonNull QueueThreadBridge renderThread(final boolean renderThread) {
		this.renderThread = renderThread;
		return this;
	}

	public boolean await(final long timeout, final @NonNull TimeUnit unit) throws InterruptedException {
		return this.posted.await(timeout, unit);
	}

	public boolean isIdle() {
		return this.tasks.isEmpty();
	}

	public void run() {
		final List<Runnable> tasks = new ArrayList<>(this.tasks);
		this.tasks.clear();
		tasks.forEach(Runnable::run);
	}

}