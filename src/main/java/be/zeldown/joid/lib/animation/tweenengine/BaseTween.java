package be.zeldown.joid.lib.animation.tweenengine;



import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class BaseTween<T> {

	// General
	private int step;
	private int repeatCnt;
	private boolean isIterationStep;
	private boolean isYoyo;

	// Timings
	protected float delay;
	protected float duration;
	private float repeatDelay;
	private float currentTime;
	private float deltaTime;
	private boolean isStarted;
	private boolean isInitialized;
	private boolean isFinished;
	private boolean isKilled;
	private boolean isPaused;

	// Misc
	private TweenCallback callback;
	private int callbackTriggers;
	private Object userData;

	// Package access
	boolean isAutoRemoveEnabled;
	boolean isAutoStartEnabled;

	// -------------------------------------------------------------------------

	protected void reset() {
		this.step = -2;
		this.repeatCnt = 0;
		this.isIterationStep = this.isYoyo = false;

		this.delay = this.duration = this.repeatDelay = this.currentTime = this.deltaTime = 0;
		this.isStarted = this.isInitialized = this.isFinished = this.isKilled = this.isPaused = false;

		this.callback = null;
		this.callbackTriggers = TweenCallback.COMPLETE;
		this.userData = null;

		this.isAutoRemoveEnabled = this.isAutoStartEnabled = true;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public T build() {
		return (T) this;
	}

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
			throw new RuntimeException("You can't change the repetitions of a tween or timeline once it is started");
		}

		this.repeatCnt = count;
		this.repeatDelay = delay >= 0 ? delay : 0;
		this.isYoyo = false;
		return (T) this;
	}

	public T repeatYoyo(final int count, final float delay) {
		if (this.isStarted) {
			throw new RuntimeException("You can't change the repetitions of a tween or timeline once it is started");
		}

		this.repeatCnt = count;
		this.repeatDelay = delay >= 0 ? delay : 0;
		this.isYoyo = true;
		return (T) this;
	}

	public T setCallback(final TweenCallback callback) {
		if (this.callback == null) {
			this.callback = callback;
		} else {
			this.callback = this.callback.andThen(callback);
		}

		return (T) this;
	}

	public T addCallback(final int flags, final Consumer<BaseTween<?>> callback) {
		this.callbackTriggers |= flags;

		return this.setCallback(((type, source) -> {
			if ((flags & type) != 0) {
				callback.accept(source);
			}
		}));
	}

	public T setCallbackTriggers(final int flags) {
		this.callbackTriggers = flags;
		return (T) this;
	}

	public T setUserData(final Object data) {
		this.userData = data;
		return (T) this;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	public float getDelay() {
		return this.delay;
	}

	public float getDuration() {
		return this.duration;
	}

	public int getRepeatCount() {
		return this.repeatCnt;
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

	public Object getUserData() {
		return this.userData;
	}

	public int getStep() {
		return this.step;
	}

	public float getCurrentTime() {
		return this.currentTime;
	}

	public boolean isStarted() {
		return this.isStarted;
	}

	public boolean isInitialized() {
		return this.isInitialized;
	}

	public boolean isFinished() {
		return this.isFinished || this.isKilled;
	}

	public boolean isYoyo() {
		return this.isYoyo;
	}

	public boolean isPaused() {
		return this.isPaused;
	}

	// -------------------------------------------------------------------------
	// Abstract API
	// -------------------------------------------------------------------------

	protected abstract void forceStartValues();

	protected abstract void forceEndValues();

	protected abstract boolean containsTarget(Object target);

	protected abstract boolean containsTarget(Object target, int tweenType);

	// -------------------------------------------------------------------------
	// Protected API
	// -------------------------------------------------------------------------

	protected void initializeOverride() {}

	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {}

	protected void forceToStart() {
		this.currentTime = -this.delay;
		this.step = -1;
		this.isIterationStep = false;
		if (this.isReverse(0)) {
			this.forceEndValues();
		} else {
			this.forceStartValues();
		}
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

	protected void callCallback(final int type) {
		if (this.callback != null && (this.callbackTriggers & type) > 0) {
			this.callback.onEvent(type, this);
		}
	}

	protected boolean isReverse(final int step) {
		return this.isYoyo && Math.abs(step % 4) == 2;
	}

	protected boolean isValid(final int step) {
		return (step >= 0 && step <= this.repeatCnt * 2) || this.repeatCnt < 0;
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

	// -------------------------------------------------------------------------
	// Update engine
	// -------------------------------------------------------------------------

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
			this.callCallback(TweenCallback.BEGIN);
			this.callCallback(TweenCallback.START);
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
			this.callCallback(TweenCallback.BEGIN);
			this.callCallback(TweenCallback.START);
			this.updateOverride(this.step, this.step - 1, this.isIterationStep, delta);
		} else if (!this.isIterationStep && this.repeatCnt >= 0 && this.step > this.repeatCnt * 2 && this.currentTime + this.deltaTime < 0) {
			assert this.step == this.repeatCnt * 2 + 1;
			this.isIterationStep = true;
			this.step = this.repeatCnt * 2;
			final float delta = 0 - this.currentTime;
			this.deltaTime -= delta;
			this.currentTime = this.duration;
			this.callCallback(TweenCallback.BACK_BEGIN);
			this.callCallback(TweenCallback.BACK_START);
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

				this.callCallback(TweenCallback.BACK_START);
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

				this.callCallback(TweenCallback.START);
				this.updateOverride(this.step, this.step - 1, this.isIterationStep, delta);
			} else if (this.isIterationStep && this.currentTime + this.deltaTime < 0) {
				this.isIterationStep = false;
				this.step -= 1;

				final float delta = 0 - this.currentTime;
				this.deltaTime -= delta;
				this.currentTime = 0;

				this.updateOverride(this.step, this.step + 1, this.isIterationStep, delta);
				this.callCallback(TweenCallback.BACK_END);

				if (this.step < 0 && this.repeatCnt >= 0) {
					this.callCallback(TweenCallback.BACK_COMPLETE);
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
				this.callCallback(TweenCallback.END);

				if (this.step > this.repeatCnt * 2 && this.repeatCnt >= 0) {
					this.callCallback(TweenCallback.COMPLETE);
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

}