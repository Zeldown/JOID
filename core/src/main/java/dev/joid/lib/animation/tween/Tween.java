package dev.joid.lib.animation.tween;



import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.animator.TweenAnimatorAccessor;
import dev.joid.lib.ui.event.EventListener;
import lombok.NonNull;

public final class Tween extends BaseTween<Tween> {

	public static final int INFINITY = -1;

	private static final Pool.Callback<Tween> poolCallback = new Pool.Callback<Tween>() {

		@Override
		public void onPool(final Tween obj) {
			obj.reset();
		}

		@Override
		public void onUnPool(final Tween obj) {
			obj.reset();
		}

	};

	private static final Pool<Tween> pool = new Pool<Tween>(20, Tween.poolCallback) {

		@Override
		protected Tween create() {
			return new Tween();
		}

	};

	private static final Map<Class<?>, TweenAccessor<?>> registeredAccessors = new HashMap<>();

	private static int combinedAttrsLimit = 3;

	static {
		Tween.registerAccessor(TweenAnimator.class, new TweenAnimatorAccessor());
	}

	private float[] waypoints = new float[0];
	private float[] startValues = new float[Tween.combinedAttrsLimit];
	private float[] targetValues = new float[Tween.combinedAttrsLimit];

	private int type;
	private Object target;
	private TweenPath path;
	private Class<?> targetClass;
	private TweenEquation equation;
	private TweenAccessor<Object> accessor;

	private boolean isFrom;
	private int waypointsCnt;
	private boolean isRelative;
	private int combinedAttrsCnt;

	private float[] pathBuffer = new float[2];
	private float[] accessorBuffer = new float[Tween.combinedAttrsLimit];

	public static Tween to(final Object target, final int tweenType, final float duration) {
		final Tween tween = Tween.pool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(TweenEquations.LINEAR);
		tween.path(TweenPaths.catmullRom);
		return tween;
	}

	public static Tween from(final Object target, final int tweenType, final float duration) {
		final Tween tween = Tween.pool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(TweenEquations.LINEAR);
		tween.path(TweenPaths.catmullRom);
		tween.isFrom = true;
		return tween;
	}

	public static Tween set(final Object target, final int tweenType) {
		final Tween tween = Tween.pool.get();
		tween.setup(target, tweenType, 0);
		tween.ease(TweenEquations.LINEAR);
		return tween;
	}

	public static Tween call(final @NonNull EventListener<TweenEvent> listener) {
		final Tween tween = Tween.pool.get();
		tween.setup(null, -1, 0);
		tween.onIterationStart(listener);
		return tween;
	}

	public static Tween mark() {
		final Tween tween = Tween.pool.get();
		tween.setup(null, -1, 0);
		return tween;
	}

	private Tween() {
		this.reset();
	}

	public static void setCombinedAttributesLimit(final int limit) {
		Tween.combinedAttrsLimit = limit;
	}

	public int getType() {
		return this.type;
	}

	public Object getTarget() {
		return this.target;
	}

	public static int getPoolSize() {
		return Tween.pool.size();
	}

	public TweenEquation getEasing() {
		return this.equation;
	}

	public Class<?> getTargetClass() {
		return this.targetClass;
	}

	public float[] getTargetValues() {
		return this.targetValues;
	}

	public static String getVersion() {
		return "6.3.3";
	}

	public TweenAccessor<?> getAccessor() {
		return this.accessor;
	}

	public int getCombinedAttributesCount() {
		return this.combinedAttrsCnt;
	}

	public static TweenAccessor<?> getRegisteredAccessor(final Class<?> someClass) {
		return Tween.registeredAccessors.get(someClass);
	}

	public static void ensurePoolCapacity(final int minCapacity) {
		Tween.pool.ensureCapacity(minCapacity);
	}

	public static void registerAccessor(final Class<?> someClass, final TweenAccessor<?> defaultAccessor) {
		Tween.registeredAccessors.put(someClass, defaultAccessor);
	}

	@Override
	protected void reset() {
		super.reset();

		this.target = null;
		this.targetClass = null;
		this.accessor = null;
		this.type = -1;
		this.equation = null;
		this.path = null;

		this.isFrom = this.isRelative = false;
		this.combinedAttrsCnt = this.waypointsCnt = 0;

		if (this.accessorBuffer.length != Tween.combinedAttrsLimit) {
			this.startValues = new float[Tween.combinedAttrsLimit];
			this.targetValues = new float[Tween.combinedAttrsLimit];
			this.accessorBuffer = new float[Tween.combinedAttrsLimit];
		}
	}

	public Tween ease(final TweenEquation easeEquation) {
		this.equation = easeEquation;
		return this;
	}

	public Tween cast(final Class<?> targetClass) {
		if (this.isStarted()) {
			throw new IllegalStateException("The target class of a tween cannot change once it is started, call cast(...) before start()");
		}

		this.targetClass = targetClass;
		return this;
	}

	public Tween target(final float targetValue) {
		this.targetValues[0] = targetValue;
		return this;
	}

	public Tween target(final float... targetValues) {
		if (targetValues.length > Tween.combinedAttrsLimit) {
			throw this.combinedAttrsLimitReached(targetValues.length);
		}

		System.arraycopy(targetValues, 0, this.targetValues, 0, targetValues.length);
		return this;
	}

	public Tween target(final float targetValue1, final float targetValue2) {
		this.targetValues[0] = targetValue1;
		this.targetValues[1] = targetValue2;
		return this;
	}

	public Tween target(final float targetValue1, final float targetValue2, final float targetValue3) {
		this.targetValues[0] = targetValue1;
		this.targetValues[1] = targetValue2;
		this.targetValues[2] = targetValue3;
		return this;
	}

	public Tween targetRelative(final float targetValue) {
		this.isRelative = true;
		this.targetValues[0] = this.isInitialized() ? targetValue + this.startValues[0] : targetValue;
		return this;
	}

	public Tween targetRelative(final float... targetValues) {
		if (targetValues.length > Tween.combinedAttrsLimit) {
			throw this.combinedAttrsLimitReached(targetValues.length);
		}

		for (int i = 0; i < targetValues.length; i++) {
			this.targetValues[i] = this.isInitialized() ? targetValues[i] + this.startValues[i] : targetValues[i];
		}

		this.isRelative = true;
		return this;
	}

	public Tween targetRelative(final float targetValue1, final float targetValue2) {
		this.isRelative = true;
		this.targetValues[0] = this.isInitialized() ? targetValue1 + this.startValues[0] : targetValue1;
		this.targetValues[1] = this.isInitialized() ? targetValue2 + this.startValues[1] : targetValue2;
		return this;
	}

	public Tween targetRelative(final float targetValue1, final float targetValue2, final float targetValue3) {
		this.isRelative = true;
		this.targetValues[0] = this.isInitialized() ? targetValue1 + this.startValues[0] : targetValue1;
		this.targetValues[1] = this.isInitialized() ? targetValue2 + this.startValues[1] : targetValue2;
		this.targetValues[2] = this.isInitialized() ? targetValue3 + this.startValues[2] : targetValue3;
		return this;
	}

	public Tween waypoint(final float targetValue) {
		this.reserveWaypoint(1);

		this.waypoints[this.waypointsCnt] = targetValue;
		this.waypointsCnt += 1;
		return this;
	}

	public Tween waypoint(final float... targetValues) {
		this.reserveWaypoint(targetValues.length);

		System.arraycopy(targetValues, 0, this.waypoints, this.waypointsCnt * targetValues.length, targetValues.length);
		this.waypointsCnt += 1;
		return this;
	}

	public Tween waypoint(final float targetValue1, final float targetValue2) {
		this.reserveWaypoint(2);

		this.waypoints[this.waypointsCnt * 2] = targetValue1;
		this.waypoints[this.waypointsCnt * 2 + 1] = targetValue2;
		this.waypointsCnt += 1;
		return this;
	}

	public Tween waypoint(final float targetValue1, final float targetValue2, final float targetValue3) {
		this.reserveWaypoint(3);

		this.waypoints[this.waypointsCnt * 3] = targetValue1;
		this.waypoints[this.waypointsCnt * 3 + 1] = targetValue2;
		this.waypoints[this.waypointsCnt * 3 + 2] = targetValue3;
		this.waypointsCnt += 1;
		return this;
	}

	public Tween path(final TweenPath path) {
		this.path = path;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Tween build() {
		if (this.target == null) {
			return this;
		}

		this.accessor = (TweenAccessor<Object>) Tween.registeredAccessors.get(this.targetClass);
		if (this.accessor == null && this.target instanceof TweenAccessor) {
			this.accessor = (TweenAccessor<Object>) this.target;
		}

		if (this.accessor != null) {
			this.combinedAttrsCnt = this.accessor.getValues(this.target, this.type, this.accessorBuffer);
		} else {
			throw new IllegalArgumentException("No TweenAccessor animates the target " + this.target + " of class " + this.target.getClass().getName() + ": register one with Tween.registerAccessor(...) or make the target implement TweenAccessor");
		}

		if (this.combinedAttrsCnt > Tween.combinedAttrsLimit) {
			throw this.combinedAttrsLimitReached(this.combinedAttrsCnt);
		}

		return this;
	}

	@Override
	protected void free() {
		Tween.pool.free(this);
	}

	@Override
	protected void initializeOverride() {
		if (this.target == null) {
			return;
		}

		this.accessor.getValues(this.target, this.type, this.startValues);

		for (int i = 0; i < this.combinedAttrsCnt; i++) {
			this.targetValues[i] += this.isRelative ? this.startValues[i] : 0;

			for (int ii = 0; ii < this.waypointsCnt; ii++) {
				this.waypoints[ii * this.combinedAttrsCnt + i] += this.isRelative ? this.startValues[i] : 0;
			}

			if (this.isFrom) {
				final float tmp = this.startValues[i];
				this.startValues[i] = this.targetValues[i];
				this.targetValues[i] = tmp;
			}
		}
	}

	@Override
	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {
		if (this.target == null || this.equation == null) {
			return;
		}

		if (!isIterationStep && step > lastStep) {
			this.accessor.setValues(this.target, this.type, this.isReverse(lastStep) ? this.startValues : this.targetValues);
			return;
		}

		if (!isIterationStep && step < lastStep) {
			this.accessor.setValues(this.target, this.type, this.isReverse(lastStep) ? this.targetValues : this.startValues);
			return;
		}

		assert isIterationStep;
		assert this.getCurrentTime() >= 0;
		assert this.getCurrentTime() <= this.duration;

		if (this.duration < 0.00000000001F && delta > -0.00000000001F) {
			this.accessor.setValues(this.target, this.type, this.isReverse(step) ? this.targetValues : this.startValues);
			return;
		}

		if (this.duration < 0.00000000001F && delta < 0.00000000001F) {
			this.accessor.setValues(this.target, this.type, this.isReverse(step) ? this.startValues : this.targetValues);
			return;
		}

		final float time = this.isReverse(step) ? this.duration - this.getCurrentTime() : this.getCurrentTime();
		final float t = this.equation.compute(time / this.duration);

		if (this.waypointsCnt == 0 || this.path == null) {
			for (int i = 0; i < this.combinedAttrsCnt; i++) {
				this.accessorBuffer[i] = this.startValues[i] + t * (this.targetValues[i] - this.startValues[i]);
			}
		} else {
			for (int i = 0; i < this.combinedAttrsCnt; i++) {
				this.pathBuffer[0] = this.startValues[i];
				this.pathBuffer[1 + this.waypointsCnt] = this.targetValues[i];
				for (int ii = 0; ii < this.waypointsCnt; ii++) {
					this.pathBuffer[ii + 1] = this.waypoints[ii * this.combinedAttrsCnt + i];
				}

				this.accessorBuffer[i] = this.path.compute(t, this.pathBuffer, this.waypointsCnt + 2);
			}
		}

		this.accessor.setValues(this.target, this.type, this.accessorBuffer);
	}

	@Override
	protected void forceEndValues() {
		if (this.target == null) {
			return;
		}

		this.accessor.setValues(this.target, this.type, this.targetValues);
	}

	@Override
	protected void forceStartValues() {
		if (this.target == null) {
			return;
		}

		this.accessor.setValues(this.target, this.type, this.startValues);
	}

	@Override
	protected boolean containsTarget(final Object target) {
		return this.target == target;
	}

	@Override
	protected boolean containsTarget(final Object target, final int tweenType) {
		return this.target == target && this.type == tweenType;
	}

	private void setup(final Object target, final int tweenType, final float duration) {
		if (duration < 0) {
			throw new IllegalArgumentException("The duration of a tween cannot be negative, got " + duration + ": pass 0 or more");
		}

		this.target = target;
		this.targetClass = target != null ? this.findTargetClass() : null;
		this.type = tweenType;
		this.duration = duration;
	}

	private Class<?> findTargetClass() {
		if (Tween.registeredAccessors.containsKey(this.target.getClass()) || (this.target instanceof TweenAccessor)) {
			return this.target.getClass();
		}

		Class<?> parentClass = this.target.getClass().getSuperclass();
		while (parentClass != null && !Tween.registeredAccessors.containsKey(parentClass)) {
			parentClass = parentClass.getSuperclass();
		}

		return parentClass;
	}

	private void reserveWaypoint(final int size) {
		if (this.waypoints.length < (this.waypointsCnt + 1) * size) {
			this.waypoints = Arrays.copyOf(this.waypoints, (this.waypointsCnt + 1) * size);
		}

		if (this.pathBuffer.length < this.waypointsCnt + 3) {
			this.pathBuffer = Arrays.copyOf(this.pathBuffer, this.waypointsCnt + 3);
		}
	}

	private IllegalArgumentException combinedAttrsLimitReached(final int count) {
		return new IllegalArgumentException("A tween combines " + count + " values but the limit is " + Tween.combinedAttrsLimit + ", raise it once at startup with Tween.setCombinedAttributesLimit(...)");
	}

}