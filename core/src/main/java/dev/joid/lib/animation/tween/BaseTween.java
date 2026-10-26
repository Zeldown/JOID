package dev.joid.lib.animation.tween;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseTween<T extends BaseTween<T>> {

	public static final int INFINITY = -1;

	private static final EventType<TweenEvent> START                   = EventType.create("onStart");
	private static final EventType<TweenEvent> COMPLETE                = EventType.create("onComplete");
	private static final EventType<TweenEvent> ITERATION_END           = EventType.create("onIterationEnd");
	private static final EventType<TweenEvent> REVERSE_START           = EventType.create("onReverseStart");
	private static final EventType<TweenEvent> ITERATION_START         = EventType.create("onIterationStart");
	private static final EventType<TweenEvent> REVERSE_COMPLETE        = EventType.create("onReverseComplete");
	private static final EventType<TweenEvent> REVERSE_ITERATION_END   = EventType.create("onReverseIterationEnd");
	private static final EventType<TweenEvent> REVERSE_ITERATION_START = EventType.create("onReverseIterationStart");

	private final EventBus eventBus = EventBus.create(() -> JOIDLog.nameOf(this.getClass()));

	@Getter private int     step;
	@Getter private boolean isYoyo;
	@Getter private int     repeatCount;
	private boolean         isIterationStep;

	private float           deltaTime;
	private boolean         isKilled;
	private boolean         isFinished;
	@Getter protected float delay;
	@Getter protected float duration;
	@Getter private boolean isPaused;
	@Getter private float   repeatDelay;
	@Getter private float   currentTime;
	@Getter private boolean isStarted;
	@Getter private boolean isInitialized;

	@Getter
	private Object userData;

	protected BaseTween() {
		this.step = -2;
	}

	public @NonNull T start() {
		this.build();
		if (this.repeatCount < 0 && this.duration + this.repeatDelay <= 0F) {
			throw new IllegalStateException("A tween or timeline that repeats forever needs a duration or a repeat delay above 0, give it one or a finite repeat count");
		}

		this.step = -2;
		this.currentTime = 0F;
		this.isKilled = false;
		this.isStarted = true;
		this.isFinished = false;
		this.isInitialized = false;
		this.isIterationStep = false;
		return (T) this;
	}

	public @NonNull T start(final @NonNull TweenManager manager) {
		manager.add(this);
		return (T) this;
	}

	public @NonNull T build() {
		return (T) this;
	}

	public @NonNull T delay(final float delay) {
		this.delay += delay;
		return (T) this;
	}

	public @NonNull T kill() {
		this.isKilled = true;
		return (T) this;
	}

	public @NonNull T pause() {
		this.isPaused = true;
		return (T) this;
	}

	public @NonNull T resume() {
		this.isPaused = false;
		return (T) this;
	}

	public @NonNull T repeat(final int count, final float delay) {
		return this.repeat(count, delay, false, "repeat");
	}

	public @NonNull T repeatYoyo(final int count, final float delay) {
		return this.repeat(count, delay, true, "repeatYoyo");
	}

	public @NonNull T userData(final Object userData) {
		this.userData = userData;
		return (T) this;
	}

	public @NonNull T onStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.START, listener);
	}

	public @NonNull T onIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.ITERATION_START, listener);
	}

	public @NonNull T onIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.ITERATION_END, listener);
	}

	public @NonNull T onComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.COMPLETE, listener);
	}

	public @NonNull T onReverseStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_START, listener);
	}

	public @NonNull T onReverseIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_ITERATION_START, listener);
	}

	public @NonNull T onReverseIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_ITERATION_END, listener);
	}

	public @NonNull T onReverseComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_COMPLETE, listener);
	}

	public float getFullDuration() {
		if (this.repeatCount < 0) {
			return -1F;
		}

		return this.delay + this.duration + (this.repeatDelay + this.duration) * this.repeatCount;
	}

	public boolean isFinished() {
		return this.isFinished || this.isKilled;
	}

	public @NonNull T forceToStart() {
		this.currentTime = -this.delay;
		this.step = -1;
		this.isIterationStep = false;
		this.forceStartValues();
		return (T) this;
	}

	public @NonNull T forceToEnd(final float time) {
		this.currentTime = time - this.getFullDuration();
		this.step = this.repeatCount * 2 + 1;
		this.isIterationStep = false;
		if (this.isReverse(this.repeatCount * 2)) {
			this.forceStartValues();
		} else {
			this.forceEndValues();
		}

		return (T) this;
	}

	public void update(final float delta) {
		if (!this.isStarted || this.isPaused || this.isKilled) {
			return;
		}

		this.deltaTime = delta;

		if (!this.isInitialized) {
			this.initialize();
		}

		if (this.isInitialized) {
			this.testRelaunch();
			this.updateStep();
			this.testCompletion();
		}

		this.currentTime += this.deltaTime;
		this.deltaTime = 0F;
	}

	protected boolean isValid(final int step) {
		return (step >= 0 && step <= this.repeatCount * 2) || this.repeatCount < 0;
	}

	protected boolean isReverse(final int step) {
		return this.isYoyo && Math.abs(step % 4) == 2;
	}

	protected abstract void forceStartValues();
	protected abstract void forceEndValues();

	protected void initializeValues() {}

	protected void updateValues(final int step, final int lastStep, final boolean isIterationStep, final float delta) {}

	private void initialize() {
		if (this.currentTime + this.deltaTime >= this.delay) {
			this.initializeValues();
			this.isInitialized = true;
			this.isIterationStep = true;
			this.step = 0;
			this.deltaTime -= this.delay - this.currentTime;
			this.currentTime = 0F;
			this.fire(BaseTween.START);
			this.fire(BaseTween.ITERATION_START);
		}
	}

	private void testRelaunch() {
		if (!this.isIterationStep && this.repeatCount >= 0 && this.step < 0 && this.currentTime + this.deltaTime >= 0F) {
			this.isIterationStep = true;
			this.step = 0;
			final float delta = 0F - this.currentTime;
			this.deltaTime -= delta;
			this.currentTime = 0F;
			this.fire(BaseTween.START);
			this.fire(BaseTween.ITERATION_START);
			this.updateValues(this.step, this.step - 1, this.isIterationStep, delta);
		} else if (!this.isIterationStep && this.repeatCount >= 0 && this.step > this.repeatCount * 2 && this.currentTime + this.deltaTime < 0F) {
			this.isIterationStep = true;
			this.step = this.repeatCount * 2;
			final float delta = 0F - this.currentTime;
			this.deltaTime -= delta;
			this.currentTime = this.duration;
			this.fire(BaseTween.REVERSE_START);
			this.fire(BaseTween.REVERSE_ITERATION_START);
			this.updateValues(this.step, this.step + 1, this.isIterationStep, delta);
		}
	}

	private void updateStep() {
		while (!this.isKilled && this.isValid(this.step)) {
			if (!this.isIterationStep && this.currentTime + this.deltaTime <= 0F) {
				this.isIterationStep = true;
				this.step -= 1;

				final float delta = 0F - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = this.duration;

				if (this.isReverse(this.step)) {
					this.forceStartValues();
				} else {
					this.forceEndValues();
				}

				this.fire(BaseTween.REVERSE_ITERATION_START);
				this.updateValues(this.step, this.step + 1, this.isIterationStep, delta);
			} else if (!this.isIterationStep && this.currentTime + this.deltaTime >= this.repeatDelay) {
				this.isIterationStep = true;
				this.step += 1;

				final float delta = this.repeatDelay - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = 0F;

				if (this.isReverse(this.step)) {
					this.forceEndValues();
				} else {
					this.forceStartValues();
				}

				this.fire(BaseTween.ITERATION_START);
				this.updateValues(this.step, this.step - 1, this.isIterationStep, delta);
			} else if (this.isIterationStep && this.currentTime + this.deltaTime < 0F) {
				this.isIterationStep = false;
				this.step -= 1;

				final float delta = 0F - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = 0F;

				this.updateValues(this.step, this.step + 1, this.isIterationStep, delta);
				this.fire(BaseTween.REVERSE_ITERATION_END);

				if (this.step < 0 && this.repeatCount >= 0) {
					this.fire(BaseTween.REVERSE_COMPLETE);
				} else {
					this.currentTime = this.repeatDelay;
				}
			} else if (this.isIterationStep && this.currentTime + this.deltaTime > this.duration) {
				this.isIterationStep = false;
				this.step += 1;

				final float delta = this.duration - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = this.duration;

				this.updateValues(this.step, this.step - 1, this.isIterationStep, delta);
				this.fire(BaseTween.ITERATION_END);

				if (this.step > this.repeatCount * 2 && this.repeatCount >= 0) {
					this.fire(BaseTween.COMPLETE);
				}

				this.currentTime = 0F;
			} else if (this.isIterationStep) {
				final float delta = this.deltaTime;
				this.deltaTime -= delta;
				this.currentTime += delta;
				this.updateValues(this.step, this.step, this.isIterationStep, delta);
				break;
			} else {
				final float delta = this.deltaTime;
				this.deltaTime -= delta;
				this.currentTime += delta;
				break;
			}
		}
	}

	private void testCompletion() {
		this.isFinished = this.isInitialized && this.repeatCount >= 0 && (this.step > this.repeatCount * 2 || this.step < 0);
	}

	private T repeat(final int count, final float delay, final boolean yoyo, final String method) {
		if (this.isStarted) {
			throw new IllegalStateException("The repetitions of a tween or timeline cannot change once it is started, call " + method + "(...) before start()");
		}

		this.repeatCount = count;
		this.repeatDelay = delay >= 0F ? delay : 0F;
		this.isYoyo = yoyo;
		return (T) this;
	}

	private T listen(final EventType<TweenEvent> type, final EventListener<TweenEvent> listener) {
		this.eventBus.listen(type, listener);
		return (T) this;
	}

	private void fire(final EventType<TweenEvent> type) {
		if (this.eventBus.has(type)) {
			this.eventBus.fire(type, TweenEvent.create(this));
		}
	}

}