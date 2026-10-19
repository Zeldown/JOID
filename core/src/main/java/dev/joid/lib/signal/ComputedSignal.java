package dev.joid.lib.signal;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.signal.replay.SignalReplayBinding;
import lombok.Getter;
import lombok.NonNull;

public class ComputedSignal<T> extends Signal<T> {

	@Getter private final transient boolean       constant;
	private final transient Supplier<? extends T> supplier;

	private transient int[]       versions;
	private transient int         dependencyCount;
	private transient Signal<?>[] dependencies;

	private transient int         collectedCount;
	private transient int[]       collectedVersions;
	private transient Signal<?>[] collectedDependencies;

	private transient long             epoch;
	private transient boolean          linked;
	private transient boolean          audible;
	private transient boolean          computed;
	private transient volatile T       value;
	private transient boolean          computing;
	private transient int              publishedVersion;
	private transient long             invalidatedEpoch;
	private transient volatile boolean stale = true;

	protected ComputedSignal(final @NonNull Supplier<? extends T> supplier) {
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
	public @NonNull ComputedSignal<T> onChange(final @NonNull SignalListener<? super T> listener) {
		final boolean listened = super.hasListeners();
		super.onChange(listener);
		this.follow();
		if (!listened) {
			this.refresh();
			this.publishedVersion = super.getVersion();
		}
		return this;
	}

	@Override
	public @NonNull ComputedSignal<T> removeOnChange(final @NonNull SignalListener<? super T> listener) {
		super.removeOnChange(listener);
		this.follow();
		return this;
	}

	@Override
	public <R> @NonNull ComputedSignal<R> map(final @NonNull Function<? super T, ? extends R> function) {
		return this.constant ? ComputedSignal.constant(function.apply(this.value)) : super.map(function);
	}

	@Override
	public T get() {
		return this.constant ? this.value : super.get();
	}

	@Override
	public T peek() {
		this.refresh();
		return this.value;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	protected synchronized void refresh() {
		if (this.constant) {
			return;
		}

		if (this.computing) {
			throw new IllegalStateException("A ComputedSignal cannot read itself while it computes its value, remove the read of this signal from its function");
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
			final boolean audible = this.audible;
			this.audible = false;
			if (!super.hasListeners()) {
				return;
			}

			this.refresh();
			if (super.getVersion() == this.publishedVersion) {
				return;
			}

			this.publishedVersion = super.getVersion();
			if (!audible) {
				return;
			}

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
		if (super.hasListeners()) {
			this.audible |= !context.isSilent();
			context.schedule(this);
		}

		final long epoch = SignalContext.getEpoch();
		if (this.invalidatedEpoch != epoch) {
			this.invalidatedEpoch = epoch;
			this.stale = true;
			super.invalidate(context);
		}
	}

	protected void depend(final Signal<?> signal) {
		if (ComputedSignal.contains(this.collectedDependencies, this.collectedCount, signal)) {
			return;
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

		if (this.collectedCount == 0 && !(this.supplier instanceof SignalReplayBinding)) {
			JOIDLog.devWarnOnce("ComputedSignal.from." + this.supplier.getClass().getName(), () -> "A Signal.from(...) of " + ComputedSignal.ownerOf(this.supplier) + " reads no signal, so its value never changes: read a signal with get() inside it, or pass a lambda to the setter to read the value at every frame");
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

	@Override
	protected @NonNull String refuse(final @NonNull String action) {
		return "The " + JOIDLog.nameOf(this.getClass()) + " is computed from other signals and cannot be " + action + ", set the signals it reads instead";
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

	private static String ownerOf(final Supplier<?> supplier) {
		final String name = supplier.getClass().getName();
		final int lambda = name.indexOf("$$Lambda");
		final String owner = lambda == -1 ? name : name.substring(0, lambda);
		return owner.substring(owner.lastIndexOf('.') + 1);
	}

	private static boolean contains(final Signal<?>[] signals, final int count, final Signal<?> signal) {
		for (int index = 0; index < count; index++) {
			if (signals[index] == signal) {
				return true;
			}
		}
		return false;
	}

}