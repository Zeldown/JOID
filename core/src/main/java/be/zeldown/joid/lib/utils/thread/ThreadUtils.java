package be.zeldown.joid.lib.utils.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.NonNull;

public final class ThreadUtils {

	public static @NonNull ThreadFactory daemonFactory(final @NonNull String name) {
		final AtomicInteger counter = new AtomicInteger();
		return task -> ThreadUtils.daemonThread(task, name + "/" + counter.incrementAndGet());
	}

	public static @NonNull Thread daemonThread(final @NonNull Runnable task, final @NonNull String name) {
		final Thread thread = new Thread(task, name);
		thread.setDaemon(true);
		return thread;
	}

}