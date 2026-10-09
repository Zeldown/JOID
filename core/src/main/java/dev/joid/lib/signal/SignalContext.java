package dev.joid.lib.signal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalContext {

	private static final AtomicLong                 EPOCH   = new AtomicLong();
	private static final ThreadLocal<SignalContext> CURRENT = ThreadLocal.withInitial(SignalContext::new);

	private final Signal<?>[]      reads        = new Signal<?>[64];
	private final Set<Signal<?>>   pendingSet   = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Deque<Signal<?>> pendingQueue = new ArrayDeque<>();

	private int               depth;
	private int               readIndex;
	private int               readCount;
	private boolean           flushing;
	private boolean           tracing = true;
	private ComputedSignal<?> observer;

	@Getter
	private long readTotal;

	public static @NonNull SignalContext current() {
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
		this.flush();
	}

	public void read(final Signal<?> signal) {
		this.readTotal++;
		if (this.observer != null) {
			this.observer.depend(signal);
		} else if (this.tracing) {
			this.record(signal);
		}
	}

	public boolean tracing(final boolean tracing) {
		final boolean previous = this.tracing;
		this.tracing = tracing;
		return previous;
	}

	public <T> T untracked(final @NonNull Supplier<T> supplier) {
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

	public @NonNull List<Signal<?>> takeReads() {
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
		if (this.depth > 0 || this.flushing) {
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
		}

		if (failure != null) {
			throw failure;
		}
	}

}