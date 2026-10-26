package dev.joid.lib.animation.tween;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.joid.lib.ui.event.EventListener;
import lombok.Getter;
import lombok.NonNull;

public final class Tween extends BaseTween<Tween> {

	private final Supplier<Float> getter;
	private final Consumer<Float> setter;

	private float                 start;
	private float                 end;
	private float                 origin;
	private float                 target;
	private ITweenPath            path;
	private int                   waypointCount;
	private float[]               waypoints;
	private float[]               pathBuffer;
	@Getter private TweenEquation easing;

	private boolean isFrom;
	private boolean isRelative;

	private Tween(final Supplier<Float> getter, final Consumer<Float> setter, final float duration) {
		if (duration < 0F) {
			throw new IllegalArgumentException("The duration of a tween cannot be negative, got " + duration + ": pass 0 or more");
		}

		this.getter     = getter;
		this.setter     = setter;
		super.duration  = duration;
		this.path       = TweenPaths.CATMULL_ROM;
		this.easing     = TweenEquations.LINEAR;
		this.waypoints  = new float[0];
		this.pathBuffer = new float[2];
	}

	public static @NonNull Tween to(final @NonNull Supplier<Float> getter, final @NonNull Consumer<Float> setter, final float duration) {
		return new Tween(getter, setter, duration);
	}

	public static @NonNull Tween from(final @NonNull Supplier<Float> getter, final @NonNull Consumer<Float> setter, final float duration) {
		final Tween tween = new Tween(getter, setter, duration);
		tween.isFrom = true;
		return tween;
	}

	public static @NonNull Tween set(final @NonNull Supplier<Float> getter, final @NonNull Consumer<Float> setter) {
		return new Tween(getter, setter, 0F);
	}

	public static @NonNull Tween call(final @NonNull EventListener<TweenEvent> listener) {
		return Tween.mark().onIterationStart(listener);
	}

	public static @NonNull Tween mark() {
		return new Tween(null, null, 0F);
	}

	public @NonNull Tween path(final @NonNull ITweenPath path) {
		this.path = path;
		return this;
	}

	public @NonNull Tween target(final float target) {
		this.target = target;
		this.resolve();
		return this;
	}

	public @NonNull Tween targetRelative(final float target) {
		this.isRelative = true;
		this.target = target;
		this.resolve();
		return this;
	}

	public @NonNull Tween waypoint(final float waypoint) {
		this.waypoints  = Arrays.copyOf(this.waypoints, this.waypointCount + 1);
		this.pathBuffer = Arrays.copyOf(this.pathBuffer, this.waypointCount + 3);

		this.waypoints[this.waypointCount] = waypoint;
		this.waypointCount++;
		return this;
	}

	public @NonNull Tween easing(final @NonNull TweenEquation easing) {
		this.easing = easing;
		return this;
	}

	@Override
	protected void initializeValues() {
		if (this.getter != null) {
			this.origin = this.getter.get();
			this.resolve();
		}
	}

	@Override
	protected void updateValues(final int step, final int lastStep, final boolean isIterationStep, final float delta) {
		if (this.setter == null) {
			return;
		}

		if (!isIterationStep && step > lastStep) {
			this.setter.accept(super.isReverse(lastStep) ? this.start : this.end);
			return;
		}

		if (!isIterationStep && step < lastStep) {
			this.setter.accept(super.isReverse(lastStep) ? this.end : this.start);
			return;
		}

		if (super.duration < 0.00000000001F && delta > -0.00000000001F) {
			this.setter.accept(super.isReverse(step) ? this.end : this.start);
			return;
		}

		if (super.duration < 0.00000000001F && delta < 0.00000000001F) {
			this.setter.accept(super.isReverse(step) ? this.start : this.end);
			return;
		}

		final float time = super.isReverse(step) ? super.duration - super.getCurrentTime() : super.getCurrentTime();
		final float progress = this.easing.compute(time / super.duration);

		if (this.waypointCount == 0) {
			this.setter.accept(this.start + progress * (this.end - this.start));
			return;
		}

		final float offset = this.isRelative ? this.origin : 0F;
		this.pathBuffer[0] = this.start;
		for (int i = 0; i < this.waypointCount; i++) {
			this.pathBuffer[i + 1] = this.waypoints[i] + offset;
		}
		this.pathBuffer[this.waypointCount + 1] = this.end;
		this.setter.accept(this.path.compute(progress, this.pathBuffer, this.waypointCount + 2));
	}

	@Override
	protected void forceStartValues() {
		if (this.setter != null) {
			this.setter.accept(this.start);
		}
	}

	@Override
	protected void forceEndValues() {
		if (this.setter != null) {
			this.setter.accept(this.end);
		}
	}

	private void resolve() {
		final float destination = this.isRelative ? this.origin + this.target : this.target;
		this.start = this.isFrom ? destination : this.origin;
		this.end   = this.isFrom ? this.origin : destination;
	}

}