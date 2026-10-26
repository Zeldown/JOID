package dev.joid.lib.animation.animator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenEvent;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalListener;
import dev.joid.lib.ui.event.EventListener;
import lombok.Getter;
import lombok.NonNull;

public class TweenAnimator extends Signal<Float> {

	private static final Set<TweenAnimator> RUNNING_SET = Collections.synchronizedSet(new HashSet<>());

	static {
		BridgeHandler.UI.onFrame(TweenAnimator::updateRunning);
	}

	@Getter
	private float speed;

	private long lastUpdate;

	@Getter
	private Timeline timeline;

	protected TweenAnimator(final float value) {
		super(value);
		this.speed = 1F;
	}

	public static @NonNull TweenAnimator create() {
		return new TweenAnimator(0F);
	}

	public static @NonNull TweenAnimator create(final float value) {
		return new TweenAnimator(value);
	}

	public @NonNull TweenAnimator start() {
		this.requireTimeline().start();
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		TweenAnimator.RUNNING_SET.add(this);
		return this;
	}

	public @NonNull TweenAnimator clear() {
		this.speed = 1F;
		return this.value(0F);
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value) {
		return this.sequence(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value, final @NonNull TweenEquation easing) {
		this.killTimeline();
		this.timeline = Timeline.createSequence();
		this.push(duration, value, easing);
		return this;
	}

	public @NonNull TweenAnimator timeline(final @NonNull Timeline timeline) {
		if (this.timeline != timeline) {
			this.killTimeline();
		}

		this.timeline = timeline;
		return this;
	}

	public @NonNull TweenAnimator speed(final float speed) {
		this.speed = speed;
		return this;
	}

	public @NonNull TweenAnimator delay(final float delay) {
		this.requireTimeline().delay(delay);
		return this;
	}

	public @NonNull TweenAnimator repeat(final int count, final float delay) {
		this.requireTimeline().repeat(count, delay);
		return this;
	}

	public @NonNull TweenAnimator repeatYoyo(final int count, final float delay) {
		this.requireTimeline().repeatYoyo(count, delay);
		return this;
	}

	public @NonNull Tween tween(final float duration) {
		return Tween.to(super::peek, this::writeValue, duration);
	}

	public @NonNull TweenAnimator push(final float duration, final float value) {
		return this.push(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator push(final float duration, final float value, final @NonNull TweenEquation easing) {
		this.requireTimeline().push(this.tween(duration).target(value).easing(easing));
		return this;
	}

	public @NonNull TweenAnimator onStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onIterationStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onIterationEnd(listener);
		return this;
	}

	public @NonNull TweenAnimator onComplete(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onComplete(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onReverseStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onReverseIterationStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onReverseIterationEnd(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseComplete(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline().onReverseComplete(listener);
		return this;
	}

	public @NonNull TweenAnimator update() {
		TweenAnimator.RUNNING_SET.remove(this);
		this.advance();
		return this;
	}

	public @NonNull TweenAnimator update(final float delta) {
		TweenAnimator.RUNNING_SET.remove(this);
		this.advance(delta);
		return this;
	}

	public @NonNull TweenAnimator value(final float value) {
		this.killTimeline();
		this.timeline = null;
		this.writeValue(value);
		return this;
	}

	@Override
	public @NonNull TweenAnimator onChange(final @NonNull SignalListener<? super Float> listener) {
		super.onChange(listener);
		return this;
	}

	@Override
	public @NonNull TweenAnimator removeOnChange(final @NonNull SignalListener<? super Float> listener) {
		super.removeOnChange(listener);
		return this;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	public boolean isAutomatic() {
		return TweenAnimator.RUNNING_SET.contains(this);
	}

	protected void writeValue(final float value) {
		super.transform(current -> value);
	}

	@Override
	protected @NonNull String refuse(final @NonNull String action) {
		return "The " + this + " is animated and cannot be " + action + ", use value(...) or start a tween";
	}

	private void killTimeline() {
		TweenAnimator.RUNNING_SET.remove(this);
		if (this.timeline != null) {
			this.timeline.kill();
		}
	}

	private void advance() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final long delta = now - this.lastUpdate;
		this.lastUpdate = now;
		this.advance(delta);
	}

	private void advance(final float delta) {
		if (this.timeline == null) {
			return;
		}

		Signal.batch(() -> this.timeline.update(delta * this.speed));
		if (this.timeline != null && this.timeline.isFinished()) {
			this.timeline = null;
			TweenAnimator.RUNNING_SET.remove(this);
		}
	}

	private Timeline requireTimeline() {
		if (this.timeline == null) {
			throw new IllegalStateException("The animator has no timeline, call sequence(...) first");
		}
		return this.timeline;
	}

	private static void updateRunning() {
		final List<TweenAnimator> animatorList;
		synchronized (TweenAnimator.RUNNING_SET) {
			animatorList = new ArrayList<>(TweenAnimator.RUNNING_SET);
		}

		for (final TweenAnimator animator : animatorList) {
			try {
				animator.advance();
			} catch (final RuntimeException exception) {
				JOIDLog.warnOnce("TweenAnimator.update." + System.identityHashCode(animator) + exception.getClass().getName(), "The update of TweenAnimator failed (" + exception + "), the other animators still run: catch the exception inside the onChange listeners of the animator or the getter and setter given to Tween.to(...) to handle it", exception);
			}
		}
	}

}