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
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class TweenAnimator extends Signal<Float> {

	private static final Set<TweenAnimator> RUNNING_SET = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	static {
		BridgeHandler.UI.onFrame(TweenAnimator::updateRunning);
	}

	private boolean automatic;

	@Setter
	private float speed;

	@Setter private long         lastUpdate;
	@Setter private Timeline     timeline;
	@Setter private TweenManager manager;

	private TweenAnimator(final float value) {
		super(value);
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

	public void clear() {
		this.release();
		this.manager.killAll();
		this.manager.update(0F);
		this.speed      = 1F;
		this.timeline   = null;
		this.lastUpdate = 0L;
		this.writeValue(0F);
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

	public @NonNull TweenAnimator push(final float duration, final float value) {
		this.push(duration, value, TweenEquations.LINEAR);
		return this;
	}

	public @NonNull TweenAnimator push(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.requireTimeline();
		this.timeline.push(Tween.to(this, TweenAnimatorAccessor.ANIMATION_VALUE, duration).target(value).ease(equation));
		return this;
	}

	public @NonNull TweenAnimator onStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onStart(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onStart(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onIterationStart(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onIterationStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onIterationStart(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.onIterationEnd(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onIterationEnd(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onIterationEnd(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.onComplete(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onComplete(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onComplete(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseStart(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onReverseStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseStart(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseIterationStart(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onReverseIterationStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseIterationStart(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseIterationEnd(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onReverseIterationEnd(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseIterationEnd(priority, listener);
		return this;
	}

	public @NonNull TweenAnimator onReverseComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseComplete(EventPriority.NORMAL, listener);
	}

	public @NonNull TweenAnimator onReverseComplete(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		this.requireTimeline();
		this.timeline.onReverseComplete(priority, listener);
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

	public void setValue(final float value) {
		this.killTimeline();
		this.timeline = null;
		this.writeValue(value);
	}

	@Override
	public @NonNull TweenAnimator reset() {
		throw new UnsupportedOperationException("A TweenAnimator is read-only and takes the value of its animation: call clear() or setValue(...) instead");
	}

	@Override
	public @NonNull TweenAnimator set(final Float value) {
		throw new UnsupportedOperationException("A TweenAnimator is read-only and takes the value of its animation: call setValue(...) instead");
	}

	@Override
	public boolean isPresent() {
		return super.get() != null;
	}

	protected void writeValue(final float value) {
		if (Float.compare(this.peek(), value) != 0) {
			super.set(value);
		}
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
				JOIDLog.warnOnce("TweenAnimator.update." + System.identityHashCode(animator) + exception, "The update of TweenAnimator failed: " + exception, exception);
			}
		}
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

}