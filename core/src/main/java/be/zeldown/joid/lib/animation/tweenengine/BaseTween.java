package be.zeldown.joid.lib.animation.tweenengine;



import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class BaseTween<T> {

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
	private int callbackTriggers;
	private TweenCallback callback;

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

	public T build() {
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

	public int getStep() {
		return this.step;
	}

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

	public float getDelay() {
		return this.delay;
	}

	public boolean isYoyo() {
		return this.isYoyo;
	}

	public boolean isPaused() {
		return this.isPaused;
	}

	public float getDuration() {
		return this.duration;
	}

	public boolean isStarted() {
		return this.isStarted;
	}

	public int getRepeatCount() {
		return this.repeatCnt;
	}

	public Object getUserData() {
		return this.userData;
	}

	public boolean isFinished() {
		return this.isFinished || this.isKilled;
	}

	public float getRepeatDelay() {
		return this.repeatDelay;
	}

	public float getCurrentTime() {
		return this.currentTime;
	}

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

	public float getFullDuration() {
		if (this.repeatCnt < 0) {
			return -1;
		}

		return this.delay + this.duration + (this.repeatDelay + this.duration) * this.repeatCnt;
	}

	public boolean isInitialized() {
		return this.isInitialized;
	}

	public T delay(final float delay) {
		this.delay += delay;
		return (T) this;
	}

	protected void initializeOverride() {}

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

	public T setUserData(final Object data) {
		this.userData = data;
		return (T) this;
	}

	protected abstract void forceEndValues();

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

	protected boolean isValid(final int step) {
		return (step >= 0 && step <= this.repeatCnt * 2) || this.repeatCnt < 0;
	}

	protected abstract void forceStartValues();

	protected void callCallback(final int type) {
		if (this.callback != null && (this.callbackTriggers & type) > 0) {
			this.callback.onEvent(type, this);
		}
	}

	protected boolean isReverse(final int step) {
		return this.isYoyo && Math.abs(step % 4) == 2;
	}

	public T setCallbackTriggers(final int flags) {
		this.callbackTriggers = flags;
		return (T) this;
	}

	protected void killTarget(final Object target) {
		if (this.containsTarget(target)) {
			this.kill();
		}
	}

	public T setCallback(final TweenCallback callback) {
		if (this.callback == null) {
			this.callback = callback;
		} else {
			this.callback = this.callback.andThen(callback);
		}

		return (T) this;
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

	protected abstract boolean containsTarget(final Object target);

	protected void killTarget(final Object target, final int tweenType) {
		if (this.containsTarget(target, tweenType)) {
			this.kill();
		}
	}

	public T addCallback(final int flags, final Consumer<BaseTween<?>> callback) {
		this.callbackTriggers |= flags;

		return this.setCallback(((type, source) -> {
			if ((flags & type) != 0) {
				callback.accept(source);
			}
		}));
	}

	protected abstract boolean containsTarget(final Object target, final int tweenType);

	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {}

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

	private void testCompletion() {
		this.isFinished = this.repeatCnt >= 0 && (this.step > this.repeatCnt * 2 || this.step < 0);
	}

}