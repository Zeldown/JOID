package dev.joid.lib.animation.tween;



import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class BaseTween<T> {

	private static final EventType<TweenEvent> START                   = EventType.create("onStart");
	private static final EventType<TweenEvent> COMPLETE                = EventType.create("onComplete");
	private static final EventType<TweenEvent> ITERATION_END           = EventType.create("onIterationEnd");
	private static final EventType<TweenEvent> REVERSE_START           = EventType.create("onReverseStart");
	private static final EventType<TweenEvent> ITERATION_START         = EventType.create("onIterationStart");
	private static final EventType<TweenEvent> REVERSE_COMPLETE        = EventType.create("onReverseComplete");
	private static final EventType<TweenEvent> REVERSE_ITERATION_END   = EventType.create("onReverseIterationEnd");
	private static final EventType<TweenEvent> REVERSE_ITERATION_START = EventType.create("onReverseIterationStart");

	private final EventBus eventBus = EventBus.create(() -> this.getClass().getSimpleName());

	private int step;
	private int repeatCnt;
	private boolean isYoyo;
	private boolean isIterationStep;

	protected float delay;
	private float deltaTime;
	protected float duration;
	private boolean isKilled;
	private boolean isPaused;
	private float repeatDelay;
	private float currentTime;
	private boolean isStarted;
	private boolean isFinished;
	private boolean isInitialized;

	private Object userData;

	protected boolean isAutoStartEnabled;
	protected boolean isAutoRemoveEnabled;

	public T start() {
		this.build();
		this.currentTime = 0;
		this.isStarted = true;
		return (T) this;
	}

	public T start(final TweenManager manager) {
		manager.add(this);
		return (T) this;
	}

	protected void reset() {
		this.step = -2;
		this.repeatCnt = 0;
		this.isIterationStep = this.isYoyo = false;

		this.delay = this.duration = this.repeatDelay = this.currentTime = this.deltaTime = 0;
		this.isStarted = this.isInitialized = this.isFinished = this.isKilled = this.isPaused = false;

		this.eventBus.clear();
		this.userData = null;

		this.isAutoRemoveEnabled = this.isAutoStartEnabled = true;
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

	public void free() {}

	public void pause() {
		this.isPaused = true;
	}

	public void resume() {
		this.isPaused = false;
	}

	public T repeat(final int count, final float delay) {
		if (this.isStarted) {
			throw new IllegalStateException("The repetitions of a tween or timeline cannot change once it is started, call repeat(...) before start()");
		}

		this.repeatCnt = count;
		this.repeatDelay = delay >= 0 ? delay : 0;
		this.isYoyo = false;
		return (T) this;
	}

	public T repeatYoyo(final int count, final float delay) {
		if (this.isStarted) {
			throw new IllegalStateException("The repetitions of a tween or timeline cannot change once it is started, call repeatYoyo(...) before start()");
		}

		this.repeatCnt = count;
		this.repeatDelay = delay >= 0 ? delay : 0;
		this.isYoyo = true;
		return (T) this;
	}

	public T setUserData(final Object data) {
		this.userData = data;
		return (T) this;
	}

	public T onStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onStart(EventPriority.NORMAL, listener);
	}

	public T onStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.START, priority, listener);
	}

	public T onIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onIterationStart(EventPriority.NORMAL, listener);
	}

	public T onIterationStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.ITERATION_START, priority, listener);
	}

	public T onIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.onIterationEnd(EventPriority.NORMAL, listener);
	}

	public T onIterationEnd(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.ITERATION_END, priority, listener);
	}

	public T onComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.onComplete(EventPriority.NORMAL, listener);
	}

	public T onComplete(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.COMPLETE, priority, listener);
	}

	public T onReverseStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseStart(EventPriority.NORMAL, listener);
	}

	public T onReverseStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_START, priority, listener);
	}

	public T onReverseIterationStart(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseIterationStart(EventPriority.NORMAL, listener);
	}

	public T onReverseIterationStart(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_ITERATION_START, priority, listener);
	}

	public T onReverseIterationEnd(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseIterationEnd(EventPriority.NORMAL, listener);
	}

	public T onReverseIterationEnd(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_ITERATION_END, priority, listener);
	}

	public T onReverseComplete(final @NonNull EventListener<TweenEvent> listener) {
		return this.onReverseComplete(EventPriority.NORMAL, listener);
	}

	public T onReverseComplete(final @NonNull EventPriority priority, final @NonNull EventListener<TweenEvent> listener) {
		return this.listen(BaseTween.REVERSE_COMPLETE, priority, listener);
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

	public Object getUserData() {
		return this.userData;
	}

	public float getCurrentTime() {
		return this.currentTime;
	}

	public float getRepeatDelay() {
		return this.repeatDelay;
	}

	public float getFullDuration() {
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

	protected abstract boolean containsTarget(final Object target);
	protected abstract boolean containsTarget(final Object target, final int tweenType);

	protected void initializeOverride() {}

	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {}

	protected void forceToStart() {
		this.currentTime = -this.delay;
		this.step = -1;
		this.isIterationStep = false;
		this.forceStartValues();
	}

	public void forceToEnd(final float time) {
		this.currentTime = time - this.getFullDuration();
		this.step = this.repeatCnt * 2 + 1;
		this.isIterationStep = false;
		if (this.isReverse(this.repeatCnt * 2)) {
			this.forceStartValues();
		} else {
			this.forceEndValues();
		}
	}

	protected void killTarget(final Object target) {
		if (this.containsTarget(target)) {
			this.kill();
		}
	}

	protected void killTarget(final Object target, final int tweenType) {
		if (this.containsTarget(target, tweenType)) {
			this.kill();
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
		while (this.isValid(this.step)) {
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
		this.isFinished = this.repeatCnt >= 0 && (this.step > this.repeatCnt * 2 || this.step < 0);
	}

	private T listen(final EventType<TweenEvent> type, final EventPriority priority, final EventListener<TweenEvent> listener) {
		this.eventBus.listen(type, priority, listener);
		return (T) this;
	}

	private void fire(final EventType<TweenEvent> type) {
		if (this.eventBus.has(type)) {
			this.eventBus.fire(type, TweenEvent.create(this));
		}
	}

}