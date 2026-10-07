package dev.joid.lib.animation.animator;

import java.util.function.Consumer;

import dev.joid.lib.animation.tweenengine.BaseTween;
import dev.joid.lib.animation.tweenengine.Timeline;
import dev.joid.lib.animation.tweenengine.Tween;
import dev.joid.lib.animation.tweenengine.TweenCallback;
import dev.joid.lib.animation.tweenengine.TweenEquation;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.animation.tweenengine.TweenManager;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class TweenAnimator {

	private float value;
	private float speed;

	private long         lastUpdate;
	private Timeline     timeline;
	private TweenManager manager;

	private TweenAnimator(final float value) {
		this.manager = new TweenManager();
		this.clear();
		this.value = value;
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
		return this;
	}

	public void clear() {
		this.manager.killAll();
		this.manager.update(0F);
		this.value      = 0F;
		this.speed      = 1F;
		this.timeline   = null;
		this.lastUpdate = 0L;
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

	public @NonNull TweenAnimator parallel(final float duration, final float value) {
		return this.parallel(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator parallel(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.killTimeline();
		this.timeline = Timeline.createParallel();
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

	public @NonNull TweenAnimator setCallback(final @NonNull Consumer<@NonNull BaseTween<@NonNull ?>> callback) {
		this.requireTimeline();
		this.timeline.addCallback(TweenCallback.END, callback);
		return this;
	}

	public @NonNull TweenAnimator update() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.update(now - this.lastUpdate);
		this.lastUpdate = now;
		return this;
	}

	public @NonNull TweenAnimator update(final float delta) {
		this.manager.update(delta * this.speed);
		if (this.timeline != null && this.timeline.isFinished()) {
			this.timeline = null;
		}
		return this;
	}

	private void killTimeline() {
		if (this.timeline != null) {
			this.timeline.kill();
		}
	}

	private void requireTimeline() {
		if (this.timeline == null) {
			throw new IllegalStateException("The animator has no timeline, call sequence(...) or parallel(...) first");
		}
	}

}