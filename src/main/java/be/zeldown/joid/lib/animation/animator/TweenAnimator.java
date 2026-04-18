package be.zeldown.joid.lib.animation.animator;

import java.util.function.Consumer;

import be.zeldown.joid.lib.animation.tweenengine.BaseTween;
import be.zeldown.joid.lib.animation.tweenengine.Timeline;
import be.zeldown.joid.lib.animation.tweenengine.Tween;
import be.zeldown.joid.lib.animation.tweenengine.TweenCallback;
import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;
import be.zeldown.joid.lib.animation.tweenengine.TweenEquations;
import be.zeldown.joid.lib.animation.tweenengine.TweenManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class TweenAnimator {

	private float value;
	private float speed;

	private TweenManager manager;
	private Timeline     timeline;
	private long         lastUpdate;

	/* [ Construct Section ] */
	private TweenAnimator(final float value) {
		this.clear();
		this.value = value;
	}

	public static @NonNull TweenAnimator create() {
		return new TweenAnimator(0F);
	}

	public static @NonNull TweenAnimator create(final float value) {
		return new TweenAnimator(value);
	}

	/* [ Manage Section ] */
	public void clear() {
		this.value      = 0F;
		this.speed      = 1F;
		this.manager    = new TweenManager();
		this.timeline   = null;
		this.lastUpdate = 0L;
	}

	/* [ Timeline Section ] */
	public @NonNull TweenAnimator sequence(final float duration, final float value) {
		return this.sequence(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator parallel(final float duration, final float value) {
		return this.parallel(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.timeline = Timeline.createSequence();
		this.push(duration, value, equation);
		return this;
	}

	public @NonNull TweenAnimator parallel(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.timeline = Timeline.createParallel();
		this.push(duration, value, equation);
		return this;
	}

	public @NonNull TweenAnimator push(final float duration, final float value) {
		this.push(duration, value, TweenEquations.LINEAR);
		return this;
	}

	public @NonNull TweenAnimator push(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.timeline.push(Tween.to(this, TweenAnimatorAccessor.ANIMATION_VALUE, duration).target(value).ease(equation));
		return this;
	}

	public @NonNull TweenAnimator setCallback(final @NonNull Consumer<@NonNull BaseTween<@NonNull ?>> callback) {
		assert this.timeline != null;
		this.timeline.addCallback(TweenCallback.END, callback);
		return this;
	}

	/* [ Start Section ] */
	public @NonNull TweenAnimator start() {
		assert this.timeline != null;
		this.lastUpdate = System.currentTimeMillis();
		this.timeline.start(this.manager);
		return this;
	}

	/* [ Update Section ] */
	public @NonNull TweenAnimator update() {
		final long now = System.currentTimeMillis();
		this.update(now - this.lastUpdate);
		this.lastUpdate = now;
		return this;
	}

	public @NonNull TweenAnimator update(final float delta) {
		this.manager.update(delta * this.speed);
		return this;
	}

}