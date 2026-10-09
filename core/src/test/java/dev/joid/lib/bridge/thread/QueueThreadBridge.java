package dev.joid.lib.bridge.thread;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public final class QueueThreadBridge implements IThreadBridge {

	private final List<Runnable> tasks = new ArrayList<>();

	private boolean renderThread;

	@Override
	public boolean isRenderThread() {
		return this.renderThread;
	}

	@Override
	public void execute(final @NonNull Runnable runnable) {
		this.tasks.add(runnable);
	}

	public @NonNull QueueThreadBridge renderThread(final boolean renderThread) {
		this.renderThread = renderThread;
		return this;
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