package dev.joid.lib.signal;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import lombok.NonNull;

public class ComputedSignal<T> extends Signal<T> {

	private final transient boolean     constant;
	private final transient Supplier<T> supplier;

	private transient int[]       versions;
	private transient int         dependencyCount;
	private transient Signal<?>[] dependencies;

	private transient int         collectedCount;
	private transient int[]       collectedVersions;
	private transient Signal<?>[] collectedDependencies;

	private transient long             epoch;
	private transient boolean          linked;
	private transient boolean          computed;
	private transient volatile T       value;
	private transient boolean          computing;
	private transient int              publishedVersion;
	private transient volatile boolean stale = true;

	protected ComputedSignal(final @NonNull Supplier<T> supplier) {
		this.constant = false;
		this.supplier = supplier;
		this.versions = new int[4];
		this.collectedVersions = new int[4];
		this.dependencies = new Signal<?>[4];
		this.collectedDependencies = new Signal<?>[4];
	}

	private ComputedSignal(final T value) {
		this.constant = true;
		this.supplier = null;
		this.value = value;
		this.computed = true;
		this.stale = false;
	}

	protected static <T> @NonNull ComputedSignal<T> constant(final T value) {
		return new ComputedSignal<>(value);
	}

	@Override
	public @NonNull ComputedSignal<T> reset() {
		throw new UnsupportedOperationException("A ComputedSignal is read-only and has no default value to reset to: reset the signals it reads instead");
	}

	@Override
	public @NonNull ComputedSignal<T> set(final T value) {
		throw new UnsupportedOperationException("A ComputedSignal is read-only and takes the value of its function: set the signals it reads instead");
	}

	@Override
	public @NonNull ComputedSignal<T> subscribe(final @NonNull ISignalSubscriber<T> subscriber) {
		final boolean subscribed = !super.getEventSet().isEmpty();
		super.subscribe(subscriber);
		this.follow();
		if (!subscribed) {
			this.refresh();
			this.publishedVersion = super.getVersion();
		}
		return this;
	}

	@Override
	public @NonNull ComputedSignal<T> unsubscribe(final @NonNull ISignalSubscriber<T> subscriber) {
		super.unsubscribe(subscriber);
		this.follow();
		return this;
	}

	@Override
	public T get() {
		return this.constant ? this.value : super.get();
	}

	@Override
	public T peek() {
		if (!this.constant) {
			this.refresh();
		}
		return this.value;
	}

	public boolean isConstant() {
		return this.constant;
	}

	@Override
	public boolean isPresent() {
		return super.get() != null;
	}

	@Override
	protected synchronized void refresh() {
		if (this.constant) {
			return;
		}

		if (this.computing) {
			throw new IllegalStateException("A ComputedSignal cannot read itself while it computes its value");
		}

		final long epoch = SignalContext.getEpoch();
		if (this.computed && (this.linked ? !this.stale : this.epoch == epoch)) {
			return;
		}

		this.stale = false;
		try {
			if (!this.computed || this.isOutdated()) {
				this.compute();
			}
		} catch (final RuntimeException exception) {
			this.stale = true;
			throw exception;
		}
		this.epoch = epoch;
	}

	@Override
	protected void dispatch() {
		final T current;
		synchronized (this) {
			if (super.getEventSet().isEmpty()) {
				return;
			}

			this.refresh();
			if (super.getVersion() == this.publishedVersion) {
				return;
			}

			this.publishedVersion = super.getVersion();
			current = this.value;
		}
		super.emit(current);
	}

	@Override
	protected synchronized void addObserver(final ComputedSignal<?> observer) {
		super.addObserver(observer);
		this.follow();
	}

	@Override
	protected synchronized void removeObserver(final ComputedSignal<?> observer) {
		super.removeObserver(observer);
		this.follow();
	}

	protected void markStale(final SignalContext context) {
		if (!super.getEventSet().isEmpty()) {
			context.schedule(this);
		}

		if (!this.stale) {
			this.stale = true;
			super.invalidate(context);
		}
	}

	protected void depend(final Signal<?> signal) {
		for (int index = 0; index < this.collectedCount; index++) {
			if (this.collectedDependencies[index] == signal) {
				return;
			}
		}

		if (this.collectedCount == this.collectedDependencies.length) {
			this.collectedDependencies = Arrays.copyOf(this.collectedDependencies, this.collectedCount * 2);
			this.collectedVersions = Arrays.copyOf(this.collectedVersions, this.collectedCount * 2);
		}
		this.collectedDependencies[this.collectedCount] = signal;
		this.collectedVersions[this.collectedCount] = signal.getVersion();
		this.collectedCount++;
	}

	private boolean isOutdated() {
		for (int index = 0; index < this.dependencyCount; index++) {
			final Signal<?> dependency = this.dependencies[index];
			dependency.refresh();
			if (dependency.getVersion() != this.versions[index]) {
				return true;
			}
		}
		return false;
	}

	private void compute() {
		final SignalContext context = SignalContext.current();
		this.collectedCount = 0;
		this.computing = true;
		final ComputedSignal<?> observer = context.observe(this);
		final T next;
		try {
			next = this.supplier.get();
		} finally {
			context.observe(observer);
			this.computing = false;
		}

		if (this.linked) {
			for (int index = 0; index < this.collectedCount; index++) {
				if (!ComputedSignal.contains(this.dependencies, this.dependencyCount, this.collectedDependencies[index])) {
					this.collectedDependencies[index].addObserver(this);
				}
			}
			for (int index = 0; index < this.dependencyCount; index++) {
				if (!ComputedSignal.contains(this.collectedDependencies, this.collectedCount, this.dependencies[index])) {
					this.dependencies[index].removeObserver(this);
				}
			}
		}

		final Signal<?>[] previousDependencies = this.dependencies;
		final int[] previousVersions = this.versions;
		final int previousCount = this.dependencyCount;
		this.dependencies = this.collectedDependencies;
		this.versions = this.collectedVersions;
		this.dependencyCount = this.collectedCount;
		this.collectedDependencies = previousDependencies;
		this.collectedVersions = previousVersions;
		this.collectedCount = 0;
		Arrays.fill(this.collectedDependencies, 0, previousCount, null);

		if (!this.computed || !Objects.equals(this.value, next)) {
			this.value = next;
			super.nextVersion();
		}
		this.computed = true;
	}

	private synchronized void follow() {
		final boolean observed = super.isObserved();
		if (observed == this.linked) {
			return;
		}

		this.linked = observed;
		for (int index = 0; index < this.dependencyCount; index++) {
			if (observed) {
				this.dependencies[index].addObserver(this);
			} else {
				this.dependencies[index].removeObserver(this);
			}
		}
		this.stale = !this.computed || this.epoch != SignalContext.getEpoch();
	}

	private static boolean contains(final Signal<?>[] signals, final int count, final Signal<?> signal) {
		for (int index = 0; index < count; index++) {
			if (signals[index] == signal) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "ComputedSignal{null}" : "ComputedSignal{" + this.peek().toString() + "}";
	}

}