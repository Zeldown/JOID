package dev.joid.lib.animation.tween;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.NonNull;

public final class Tween extends BaseTween<Tween> {

	public static final int INFINITY = -1;

	private final Supplier<Float> getter;
	private final Consumer<Float> setter;

	private float         start;
	private float         target;
	private TweenPath     path;
	private int           waypointCount;
	private TweenEquation equation;
	private float[]       waypoints  = new float[0];
	private float[]       pathBuffer = new float[2];

	private Tween(final Supplier<Float> getter, final Consumer<Float> setter, final float duration) {
		if (duration < 0) {
			throw new IllegalArgumentException("The duration of a tween cannot be negative, got " + duration + ": pass 0 or more");
		}

		this.getter   = getter;
		this.setter   = setter;
		this.duration = duration;
		this.path     = TweenPaths.catmullRom;
		this.equation = TweenEquations.LINEAR;
	}

	public static @NonNull Tween to(final @NonNull Supplier<Float> getter, final @NonNull Consumer<Float> setter, final float duration) {
		return new Tween(getter, setter, duration);
	}

	public static @NonNull Tween mark() {
		return new Tween(null, null, 0F);
	}

	public @NonNull Tween path(final @NonNull TweenPath path) {
		this.path = path;
		return this;
	}

	public @NonNull Tween target(final float target) {
		this.target = target;
		return this;
	}

	public @NonNull Tween waypoint(final float waypoint) {
		if (this.waypoints.length == this.waypointCount) {
			this.waypoints  = Arrays.copyOf(this.waypoints, this.waypointCount + 1);
			this.pathBuffer = Arrays.copyOf(this.pathBuffer, this.waypointCount + 3);
		}

		this.waypoints[this.waypointCount] = waypoint;
		this.waypointCount++;
		return this;
	}

	public @NonNull Tween ease(final @NonNull TweenEquation equation) {
		this.equation = equation;
		return this;
	}

	@Override
	protected void initializeOverride() {
		if (this.getter != null) {
			this.start = this.getter.get();
		}
	}

	@Override
	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {
		if (this.setter == null) {
			return;
		}

		if (!isIterationStep && step > lastStep) {
			this.setter.accept(this.isReverse(lastStep) ? this.start : this.target);
			return;
		}

		if (!isIterationStep && step < lastStep) {
			this.setter.accept(this.isReverse(lastStep) ? this.target : this.start);
			return;
		}

		if (this.duration < 0.00000000001F && delta > -0.00000000001F) {
			this.setter.accept(this.isReverse(step) ? this.target : this.start);
			return;
		}

		if (this.duration < 0.00000000001F && delta < 0.00000000001F) {
			this.setter.accept(this.isReverse(step) ? this.start : this.target);
			return;
		}

		final float time = this.isReverse(step) ? this.duration - this.getCurrentTime() : this.getCurrentTime();
		final float progress = this.equation.compute(time / this.duration);

		if (this.waypointCount == 0) {
			this.setter.accept(this.start + progress * (this.target - this.start));
			return;
		}

		this.pathBuffer[0] = this.start;
		System.arraycopy(this.waypoints, 0, this.pathBuffer, 1, this.waypointCount);
		this.pathBuffer[this.waypointCount + 1] = this.target;
		this.setter.accept(this.path.compute(progress, this.pathBuffer, this.waypointCount + 2));
	}

	@Override
	protected void forceEndValues() {
		if (this.setter != null) {
			this.setter.accept(this.target);
		}
	}

	@Override
	protected void forceStartValues() {
		if (this.setter != null) {
			this.setter.accept(this.start);
		}
	}

}