package dev.joid.lib.animation.tween;



import dev.joid.internal.JOIDLog;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import lombok.NonNull;

public abstract class BaseTween<T> {

	private static final EventType<TweenEvent> START                   = EventType.create("onStart");
	private static final EventType<TweenEvent> COMPLETE                = EventType.create("onComplete");
	private static final EventType<TweenEvent> ITERATION_END           = EventType.create("onIterationEnd");
	private static final EventType<TweenEvent> REVERSE_START           = EventType.create("onReverseStart");
	private static final EventType<TweenEvent> ITERATION_START         = EventType.create("onIterationStart");
	private static final EventType<TweenEvent> REVERSE_COMPLETE        = EventType.create("onReverseComplete");
	private static final EventType<TweenEvent> REVERSE_ITERATION_END   = EventType.create("onReverseIterationEnd");
	private static final EventType<TweenEvent> REVERSE_ITERATION_START = EventType.create("onReverseIterationStart");

	private final EventBus eventBus = EventBus.create(() -> JOIDLog.nameOf(this.getClass()));

	private int     step = -2;
	private int     repeatCnt;
	private boolean isYoyo;
	private boolean isIterationStep;

	protected float delay;
	private float   deltaTime;
	protected float duration;
	private boolean isKilled;
	private boolean isPaused;
	private float   repeatDelay;
	private float   currentTime;
	private boolean isStarted;
	private boolean isFinished;
	private boolean isInitialized;

	public T start() {
		this.build();
		if (this.repeatCnt < 0 && this.duration + this.repeatDelay <= 0) {
			throw new IllegalStateException("A tween or timeline that repeats forever needs a duration or a repeat delay above 0, give it one or a finite repeat count");
		}

		this.step = -2;
		this.currentTime = 0;
		this.isKilled = false;
		this.isStarted = true;
		this.isFinished = false;
		this.isInitialized = false;
		this.isIterationStep = false;
		return (T) this;
	}

	public T start(final @NonNull TweenManager manager) {
		manager.add(this);
		return (T) this;
	}

	public T build() {
		return (T) this;
	}

	public T delay(final float delay) {
		this.delay += delay;
		return (T) this;
	}

	public void kill() {
		this.isKilled = true;
	}

	public void pause() {
		this.isPaused = true;
	}

	public void resume() {
		this.isPaused = false;
	}

	public T repeat(final int count, final float delay) {
		return this.repeat(count, delay, false, "repeat");
	}

	public T repeatYoyo(final int count, final float delay) {
		return this.repeat(count, delay, true, "repeatYoyo");
	}

	public T onStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.START, listener);
	}

	public T onIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.ITERATION_START, listener);
	}

	public T onIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.ITERATION_END, listener);
	}

	public T onComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.COMPLETE, listener);
	}

	public T onReverseStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_START, listener);
	}

	public T onReverseIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_ITERATION_START, listener);
	}

	public T onReverseIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_ITERATION_END, listener);
	}

	public T onReverseComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_COMPLETE, listener);
	}

	public int getStep() {
		return this.step;
	}

	public float getDelay() {
		return this.delay;
	}

	public float getDuration() {
		return this.duration;
	}

	public int getRepeatCount() {
		return this.repeatCnt;
	}

	public float getCurrentTime() {
		return this.currentTime;
	}

	public float getRepeatDelay() {
		return this.repeatDelay;
	}

	protected float getFullDuration() {
		if (this.repeatCnt < 0) {
			return -1;
		}

		return this.delay + this.duration + (this.repeatDelay + this.duration) * this.repeatCnt;
	}

	public boolean isYoyo() {
		return this.isYoyo;
	}

	public boolean isPaused() {
		return this.isPaused;
	}

	public boolean isStarted() {
		return this.isStarted;
	}

	public boolean isFinished() {
		return this.isFinished || this.isKilled;
	}

	public boolean isInitialized() {
		return this.isInitialized;
	}

	protected boolean isValid(final int step) {
		return (step >= 0 && step <= this.repeatCnt * 2) || this.repeatCnt < 0;
	}

	protected boolean isReverse(final int step) {
		return this.isYoyo && Math.abs(step % 4) == 2;
	}

	protected abstract void forceEndValues();
	protected abstract void forceStartValues();

	protected void initializeOverride() {}

	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {}

	protected void forceToStart() {
		this.currentTime = -this.delay;
		this.step = -1;
		this.isIterationStep = false;
		this.forceStartValues();
	}

	protected void forceToEnd(final float time) {
		this.currentTime = time - this.getFullDuration();
		this.step = this.repeatCnt * 2 + 1;
		this.isIterationStep = false;
		if (this.isReverse(this.repeatCnt * 2)) {
			this.forceStartValues();
		} else {
			this.forceEndValues();
		}
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
		this.deltaTime = 0;
	}

	private void initialize() {
		if (this.currentTime + this.deltaTime >= this.delay) {
			this.initializeOverride();
			this.isInitialized = true;
			this.isIterationStep = true;
			this.step = 0;
			this.deltaTime -= this.delay - this.currentTime;
			this.currentTime = 0;
			this.fire(BaseTween.START);
			this.fire(BaseTween.ITERATION_START);
		}
	}

	private void testRelaunch() {
		if (!this.isIterationStep && this.repeatCnt >= 0 && this.step < 0 && this.currentTime + this.deltaTime >= 0) {
			assert this.step == -1;
			this.isIterationStep = true;
			this.step = 0;
			final float delta = 0 - this.currentTime;
			this.deltaTime -= delta;
			this.currentTime = 0;
			this.fire(BaseTween.START);
			this.fire(BaseTween.ITERATION_START);
			this.updateOverride(this.step, this.step - 1, this.isIterationStep, delta);
		} else if (!this.isIterationStep && this.repeatCnt >= 0 && this.step > this.repeatCnt * 2 && this.currentTime + this.deltaTime < 0) {
			assert this.step == this.repeatCnt * 2 + 1;
			this.isIterationStep = true;
			this.step = this.repeatCnt * 2;
			final float delta = 0 - this.currentTime;
			this.deltaTime -= delta;
			this.currentTime = this.duration;
			this.fire(BaseTween.REVERSE_START);
			this.fire(BaseTween.REVERSE_ITERATION_START);
			this.updateOverride(this.step, this.step + 1, this.isIterationStep, delta);
		}
	}

	private void updateStep() {
		while (!this.isKilled && this.isValid(this.step)) {
			if (!this.isIterationStep && this.currentTime + this.deltaTime <= 0) {
				this.isIterationStep = true;
				this.step -= 1;

				final float delta = 0 - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = this.duration;

				if (this.isReverse(this.step)) {
					this.forceStartValues();
				} else {
					this.forceEndValues();
				}

				this.fire(BaseTween.REVERSE_ITERATION_START);
				this.updateOverride(this.step, this.step + 1, this.isIterationStep, delta);
			} else if (!this.isIterationStep && this.currentTime + this.deltaTime >= this.repeatDelay) {
				this.isIterationStep = true;
				this.step += 1;

				final float delta = this.repeatDelay - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = 0;

				if (this.isReverse(this.step)) {
					this.forceEndValues();
				} else {
					this.forceStartValues();
				}

				this.fire(BaseTween.ITERATION_START);
				this.updateOverride(this.step, this.step - 1, this.isIterationStep, delta);
			} else if (this.isIterationStep && this.currentTime + this.deltaTime < 0) {
				this.isIterationStep = false;
				this.step -= 1;

				final float delta = 0 - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = 0;

				this.updateOverride(this.step, this.step + 1, this.isIterationStep, delta);
				this.fire(BaseTween.REVERSE_ITERATION_END);

				if (this.step < 0 && this.repeatCnt >= 0) {
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

				this.updateOverride(this.step, this.step - 1, this.isIterationStep, delta);
				this.fire(BaseTween.ITERATION_END);

				if (this.step > this.repeatCnt * 2 && this.repeatCnt >= 0) {
					this.fire(BaseTween.COMPLETE);
				}

				this.currentTime = 0;
			} else if (this.isIterationStep) {
				final float delta = this.deltaTime;
				this.deltaTime -= delta;
				this.currentTime += delta;
				this.updateOverride(this.step, this.step, this.isIterationStep, delta);
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
		this.isFinished = this.isInitialized && this.repeatCnt >= 0 && (this.step > this.repeatCnt * 2 || this.step < 0);
	}

	private T repeat(final int count, final float delay, final boolean yoyo, final String method) {
		if (this.isStarted) {
			throw new IllegalStateException("The repetitions of a tween or timeline cannot change once it is started, call " + method + "(...) before start()");
		}

		this.repeatCnt = count;
		this.repeatDelay = delay >= 0 ? delay : 0;
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