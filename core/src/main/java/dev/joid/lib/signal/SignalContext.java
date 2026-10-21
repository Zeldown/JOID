package dev.joid.lib.signal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalContext {

	private static final AtomicLong                 EPOCH   = new AtomicLong();
	private static final ThreadLocal<SignalContext> CURRENT = ThreadLocal.withInitial(SignalContext::new);

	private final Signal<?>[]             reads        = new Signal<?>[64];
	private final Set<Signal<?>>          pendingSet   = Collections.newSetFromMap(new IdentityHashMap<>());
	private final List<Runnable>          deferredList = new ArrayList<>();
	private final Deque<Runnable>         settleQueue  = new ArrayDeque<>();
	private final Deque<Signal<?>>        pendingQueue = new ArrayDeque<>();
	private final Map<Signal<?>, Object>  originMap    = new IdentityHashMap<>();
	private final Map<Signal<?>, Integer> writeBackMap = new IdentityHashMap<>();

	private int               depth;
	private int               readIndex;
	private int               readCount;
	private boolean           tracing;
	private boolean           flushing;
	private ComputedSignal<?> observer;

	@Getter private long    readTotal;
	@Getter private boolean silent;

	public static SignalContext current() {
		return SignalContext.CURRENT.get();
	}

	public static long getEpoch() {
		return SignalContext.EPOCH.get();
	}

	public static long nextEpoch() {
		return SignalContext.EPOCH.incrementAndGet();
	}

	public void open() {
		this.depth++;
	}

	public void close() {
		this.depth--;
		if (this.depth == 0 && !this.deferredList.isEmpty()) {
			final List<Runnable> taskList = new ArrayList<>(this.deferredList);
			this.deferredList.clear();
			ThreadUtils.runOnRenderThread(() -> Signal.batch(() -> taskList.forEach(Runnable::run)));
		}
		this.flush();
	}

	public void defer(final Runnable task) {
		final Runnable deferred = this.silent ? () -> Signal.silent(task) : task;
		if (this.depth > 0) {
			this.deferredList.add(deferred);
		} else {
			ThreadUtils.runOnRenderThread(deferred);
		}
	}

	public void settle(final Runnable task) {
		if (this.flushing) {
			final boolean silent = this.silent;
			this.settleQueue.add(() -> {
				final boolean previous = this.silent(silent);
				try {
					task.run();
				} finally {
					this.silent(previous);
				}
			});
		} else {
			task.run();
		}
	}

	public void read(final Signal<?> signal) {
		this.readTotal++;
		if (this.observer != null) {
			this.observer.depend(signal);
		} else if (this.tracing) {
			this.record(signal);
		}
	}

	public boolean silent(final boolean silent) {
		final boolean previous = this.silent;
		this.silent = silent;
		return previous;
	}

	public boolean tracing(final boolean tracing) {
		final boolean previous = this.tracing;
		this.tracing = tracing;
		return previous;
	}

	public <T> T untracked(final Supplier<? extends T> supplier) {
		final ComputedSignal<?> observer = this.observe(null);
		final boolean tracing = this.tracing(false);
		try {
			return supplier.get();
		} finally {
			this.tracing(tracing);
			this.observe(observer);
		}
	}

	public ComputedSignal<?> observe(final ComputedSignal<?> observer) {
		final ComputedSignal<?> previous = this.observer;
		this.observer = observer;
		return previous;
	}

	public void schedule(final Signal<?> signal) {
		if (this.pendingSet.add(signal)) {
			this.pendingQueue.add(signal);
		}
	}

	public int countWriteBack(final Signal<?> signal) {
		if (!this.flushing) {
			return 0;
		}

		if (!this.originMap.containsKey(signal)) {
			this.originMap.put(signal, signal.peek());
		}
		return this.writeBackMap.merge(signal, 1, Integer::sum);
	}

	public Object getWriteBackOrigin(final Signal<?> signal) {
		return this.originMap.get(signal);
	}

	public List<Signal<?>> takeReads() {
		final List<Signal<?>> readList = new ArrayList<>(this.readCount);
		for (int index = this.readCount; index > 0; index--) {
			final int position = this.readIndex - index & this.reads.length - 1;
			readList.add(this.reads[position]);
			this.reads[position] = null;
		}
		this.readCount = 0;
		return readList;
	}

	public void putBackReads(final List<Signal<?>> readList) {
		final List<Signal<?>> recentList = this.takeReads();
		readList.forEach(this::record);
		recentList.forEach(this::record);
	}

	public void clearReads() {
		while (this.readCount > 0) {
			this.reads[this.readIndex - this.readCount & this.reads.length - 1] = null;
			this.readCount--;
		}
	}

	public boolean hasReads() {
		return this.readCount > 0;
	}

	private void record(final Signal<?> signal) {
		this.reads[this.readIndex] = signal;
		this.readIndex = this.readIndex + 1 & this.reads.length - 1;
		if (this.readCount < this.reads.length) {
			this.readCount++;
		}
	}

	private void flush() {
		if (this.depth > 0 || this.flushing || this.pendingQueue.isEmpty()) {
			return;
		}

		RuntimeException failure = null;
		this.flushing = true;
		try {
			while (!this.pendingQueue.isEmpty()) {
				final Signal<?> signal = this.pendingQueue.poll();
				this.pendingSet.remove(signal);
				try {
					signal.dispatch();
				} catch (final RuntimeException exception) {
					if (failure == null) {
						failure = exception;
					} else {
						failure.addSuppressed(exception);
					}
				}
			}
		} finally {
			this.flushing = false;
			this.originMap.clear();
			this.writeBackMap.clear();
		}

		while (!this.settleQueue.isEmpty()) {
			try {
				this.settleQueue.poll().run();
			} catch (final RuntimeException exception) {
				if (failure == null) {
					failure = exception;
				} else {
					failure.addSuppressed(exception);
				}
			}
		}

		if (failure != null) {
			throw failure;
		}
	}

}