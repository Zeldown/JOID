package dev.joid.lib.animation.animator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenEvent;
import dev.joid.lib.animation.tween.TweenManager;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.signal.ISignalListener;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class TweenAnimator extends Signal<Float> {

	private static final Set<TweenAnimator> RUNNING_SET = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	static {
		BridgeHandler.UI.onFrame(TweenAnimator::updateRunning);
	}

	@Getter
	private boolean automatic;

	@Getter
	@Setter
	private float speed;

	private long         lastUpdate;
	private TweenManager manager;

	private volatile float value;

	@Getter
	private Timeline timeline;

	protected TweenAnimator(final float value) {
		this.value = value;
		this.manager = new TweenManager();
		this.speed = 1F;
	}

	public static @NonNull TweenAnimator create() {
		return new TweenAnimator(0F);
	}

	public static @NonNull TweenAnimator create(final float value) {
		return new TweenAnimator(value);
	}

	public @NonNull TweenAnimator start() {
		this.requireTimeline();
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.timeline.start(this.manager);
		this.automatic = UI.getCurrent() != null;
		if (this.automatic) {
			TweenAnimator.RUNNING_SET.add(this);
		} else {
			TweenAnimator.RUNNING_SET.remove(this);
		}
		return this;
	}

	public @NonNull TweenAnimator clear() {
		this.release();
		this.manager.killAll();
		this.manager.update(0F);
		this.speed      = 1F;
		this.timeline   = null;
		this.lastUpdate = 0L;
		this.writeValue(0F);
		return this;
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value) {
		return this.sequence(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.killTimeline();
		this.timeline = Timeline.createSequence();
		this.push(duration, value, equation);
		return this;
	}

	public @NonNull TweenAnimator timeline(final @NonNull Timeline timeline) {
		if (this.timeline != timeline) {
			this.killTimeline();
		}

		this.timeline = timeline;
		return this;
	}

	public @NonNull Tween tween(final float duration) {
		return Tween.to(this::peek, this::writeValue, duration);
	}

	public @NonNull TweenAnimator push(final float duration, final float value) {
		this.push(duration, value, TweenEquations.LINEAR);
		return this;
	}

	public @NonNull TweenAnimator push(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.requireTimeline();
		this.timeline.push(this.tween(duration).target(value).ease(equation));
		return this;
	}

	public @NonNull TweenAnimator onStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onIterationStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onIterationEnd(listener);
		return this;
	}

	public @NonNull TweenAnimator onComplete(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onComplete(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseIterationStart(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseIterationEnd(listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseComplete(final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseComplete(listener);
		return this;
	}

	public @NonNull TweenAnimator update() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final long delta = now - this.lastUpdate;
		this.lastUpdate = now;
		return this.update(delta);
	}

	public @NonNull TweenAnimator update(final float delta) {
		Signal.batch(() -> this.manager.update(delta * this.speed));
		if (this.timeline != null && this.timeline.isFinished()) {
			this.timeline = null;
			this.release();
		}
		return this;
	}

	public @NonNull TweenAnimator value(final float value) {
		this.killTimeline();
		this.timeline = null;
		this.writeValue(value);
		return this;
	}

	@Override
	public @NonNull TweenAnimator set(final Float value) {
		throw new IllegalStateException(this.refuse("set to " + value));
	}

	@Override
	public @NonNull TweenAnimator reset() {
		throw new IllegalStateException(this.refuse("reset"));
	}

	@Override
	public @NonNull TweenAnimator onChange(final @NonNull ISignalListener<? super Float> listener) {
		super.onChange(listener);
		return this;
	}

	@Override
	public @NonNull TweenAnimator removeOnChange(final @NonNull ISignalListener<? super Float> listener) {
		super.removeOnChange(listener);
		return this;
	}

	@Override
	public @NonNull Float peek() {
		return this.value;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	protected void writeValue(final float value) {
		super.write(() -> {
			if (Float.compare(this.value, value) != 0) {
				this.value = value;
				super.propagate(true);
			}
		});
	}

	private String refuse(final String action) {
		return "The " + this + " is animated and cannot be " + action + ", use value(...) or start a tween";
	}

	private void release() {
		this.automatic = false;
		TweenAnimator.RUNNING_SET.remove(this);
	}

	private void killTimeline() {
		this.release();
		if (this.timeline != null) {
			this.timeline.kill();
		}
	}

	private void requireTimeline() {
		if (this.timeline == null) {
			throw new IllegalStateException("The animator has no timeline, call sequence(...) first");
		}
	}

	private static void updateRunning() {
		final List<TweenAnimator> animatorList;
		synchronized (TweenAnimator.RUNNING_SET) {
			animatorList = new ArrayList<>(TweenAnimator.RUNNING_SET);
		}

		for (final TweenAnimator animator : animatorList) {
			try {
				animator.update();
			} catch (final RuntimeException exception) {
				JOIDLog.warnOnce("TweenAnimator.update." + System.identityHashCode(animator) + exception, "The update of TweenAnimator failed (" + exception + "), the other animators still run: catch the exception inside the tween callbacks to handle it", exception);
			}
		}
	}

}