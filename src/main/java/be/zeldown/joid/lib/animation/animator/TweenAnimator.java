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

	/**
	 * Creates a new TweenAnimator with an initial value of 0.
	 *
	 * @return The created TweenAnimator instance.
	 */
	public static @NonNull TweenAnimator create() {
		return new TweenAnimator(0F);
	}

	/**
	 * Creates a new TweenAnimator with a specified initial value.
	 *
	 * @param value The initial value of the animator.
	 * @return The created TweenAnimator instance.
	 */
	public static @NonNull TweenAnimator create(final float value) {
		return new TweenAnimator(value);
	}

	/* [ Manage Section ] */
	/**
	 * Clears the current state of the animator, resetting values and removing any ongoing animations.
	 */
	public void clear() {
		this.value      = 0F;
		this.speed      = 1F;
		this.manager    = new TweenManager();
		this.timeline   = null;
		this.lastUpdate = 0L;
	}

	/* [ Timeline Section ] */
	/**
	 * Configures the TweenAnimator to execute a sequence of animations with the specified duration and target value.
	 * The default easing equation used is {@link TweenEquations#LINEAR}.
	 *
	 * @param duration The duration of the animation sequence.
	 * @param value The target value of the animation.
	 * @return This TweenAnimator instance for method chaining.
	 */
	public @NonNull TweenAnimator sequence(final float duration, final float value) {
		return this.sequence(duration, value, TweenEquations.LINEAR);
	}

	/**
	 * Configures the TweenAnimator to execute a parallel set of animations with the specified duration and target value.
	 * The default easing equation used is {@link TweenEquations#LINEAR}.
	 *
	 * @param duration The duration of the parallel animations.
	 * @param value The target value of the parallel animations.
	 * @return This TweenAnimator instance for method chaining.
	 */
	public @NonNull TweenAnimator parallel(final float duration, final float value) {
		return this.parallel(duration, value, TweenEquations.LINEAR);
	}

	/**
	 * Configures the TweenAnimator to execute a sequence of animations with the specified duration, target value, and easing equation.
	 *
	 * @param duration The duration of the animation sequence.
	 * @param value The target value of the animation.
	 * @param equation The easing equation to be used for the animation.
	 * @throws NullPointerException If the provided equation is {@code null}.
	 * @return This TweenAnimator instance for method chaining.
	 */
	public @NonNull TweenAnimator sequence(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.timeline = Timeline.createSequence();
		this.push(duration, value, equation);
		return this;
	}

	/**
	 * Configures the TweenAnimator to execute a parallel set of animations with the specified duration, target value, and easing equation.
	 *
	 * @param duration The duration of the parallel animations.
	 * @param value The target value of the parallel animations.
	 * @param equation The easing equation to be used for the animations.
	 * @throws NullPointerException If the provided equation is {@code null}.
	 * @return This TweenAnimator instance for method chaining.
	 */
	public @NonNull TweenAnimator parallel(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.timeline = Timeline.createParallel();
		this.push(duration, value, equation);
		return this;
	}

	/**
	 * Adds an animation to the current animation sequence with the specified duration and target value.
	 * The default easing equation used is {@link TweenEquations#LINEAR}.
	 *
	 * @param duration The duration of the animation.
	 * @param value The target value of the animation.
	 */
	public void push(final float duration, final float value) {
		this.push(duration, value, TweenEquations.LINEAR);
	}

	/**
	 * Adds an animation to the current animation sequence with the specified duration, target value, and easing equation.
	 *
	 * @param duration The duration of the animation.
	 * @param value The target value of the animation.
	 * @param equation The easing equation to be used for the animation.
	 * @throws NullPointerException If the provided equation is {@code null}.
	 */
	public void push(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.timeline.push(Tween.to(this, TweenAnimatorAccessor.ANIMATION_VALUE, duration).target(value).ease(equation));
	}

	/**
	 * Sets a callback to be executed when the animation ends.
	 *
	 * @param callback The callback function to be executed.
	 * @throws NullPointerException If the provided callback is {@code null}.
	 * @return This TweenAnimator instance for method chaining.
	 */
	public @NonNull TweenAnimator setCallback(final @NonNull Consumer<@NonNull BaseTween<@NonNull ?>> callback) {
		assert this.timeline != null;
		this.timeline.addCallback(TweenCallback.END, callback);
		return this;
	}

	/* [ Start Section ] */
	/**
	 * Starts the animation. This method must be called after creating the animation sequence or parallel set.
	 *
	 * @return This TweenAnimator instance for method chaining.
	 */
	public @NonNull TweenAnimator start() {
		assert this.timeline != null;
		this.lastUpdate = System.currentTimeMillis();
		this.timeline.start(this.manager);
		return this;
	}

	/* [ Update Section ] */
	/**
	 * Updates the TweenAnimator, advancing the animation progress based on the elapsed time since the last update.
	 * The internal TweenManager is updated with the adjusted delta time.
	 */
	public void update() {
		final long now = System.currentTimeMillis();
		this.update(now - this.lastUpdate);
		this.lastUpdate = now;
	}

	/**
	 * Updates the TweenAnimator with the specified delta time, advancing the animation progress.
	 * The internal TweenManager is updated with the adjusted delta time scaled by the animation speed.
	 *
	 * @param delta The time elapsed since the last update, adjusted by the animation speed.
	 */
	public void update(final float delta) {
		this.manager.update(delta * this.speed);
	}

}