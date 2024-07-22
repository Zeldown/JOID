package be.zeldown.joid.lib.animation.tweenengine;



import java.util.HashMap;
import java.util.Map;

import be.zeldown.joid.lib.animation.animator.TweenAnimator;
import be.zeldown.joid.lib.animation.animator.TweenAnimatorAccessor;
import be.zeldown.joid.lib.animation.tweenengine.equation.Quad;

public final class Tween extends BaseTween<Tween> {

	// -------------------------------------------------------------------------
	// Static -- misc
	// -------------------------------------------------------------------------

	public static final int INFINITY = -1;

	private static int combinedAttrsLimit = 3;
	private static int waypointsLimit = 0;

	public static void setCombinedAttributesLimit(final int limit) {
		Tween.combinedAttrsLimit = limit;
	}

	public static void setWaypointsLimit(final int limit) {
		Tween.waypointsLimit = limit;
	}

	public static String getVersion() {
		return "6.3.3";
	}

	// -------------------------------------------------------------------------
	// Static -- pool
	// -------------------------------------------------------------------------

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

	public static int getPoolSize() {
		return Tween.pool.size();
	}

	public static void ensurePoolCapacity(final int minCapacity) {
		Tween.pool.ensureCapacity(minCapacity);
	}

	// -------------------------------------------------------------------------
	// Static -- tween accessors
	// -------------------------------------------------------------------------

	private static final Map<Class<?>, TweenAccessor<?>> registeredAccessors = new HashMap<>();

	static {
		Tween.registerAccessor(TweenAnimator.class, new TweenAnimatorAccessor());
	}

	public static void registerAccessor(final Class<?> someClass, final TweenAccessor<?> defaultAccessor) {
		Tween.registeredAccessors.put(someClass, defaultAccessor);
	}

	public static TweenAccessor<?> getRegisteredAccessor(final Class<?> someClass) {
		return Tween.registeredAccessors.get(someClass);
	}

	// -------------------------------------------------------------------------
	// Static -- factories
	// -------------------------------------------------------------------------

	public static Tween to(final Object target, final int tweenType, final float duration) {
		final Tween tween = Tween.pool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(Quad.INOUT);
		tween.path(TweenPaths.catmullRom);
		return tween;
	}

	public static Tween from(final Object target, final int tweenType, final float duration) {
		final Tween tween = Tween.pool.get();
		tween.setup(target, tweenType, duration);
		tween.ease(Quad.INOUT);
		tween.path(TweenPaths.catmullRom);
		tween.isFrom = true;
		return tween;
	}

	public static Tween set(final Object target, final int tweenType) {
		final Tween tween = Tween.pool.get();
		tween.setup(target, tweenType, 0);
		tween.ease(Quad.INOUT);
		return tween;
	}

	public static Tween call(final TweenCallback callback) {
		final Tween tween = Tween.pool.get();
		tween.setup(null, -1, 0);
		tween.setCallback(callback);
		tween.setCallbackTriggers(TweenCallback.START);
		return tween;
	}

	public static Tween mark() {
		final Tween tween = Tween.pool.get();
		tween.setup(null, -1, 0);
		return tween;
	}

	// -------------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------------

	// Main
	private Object target;
	private Class<?> targetClass;
	private TweenAccessor<Object> accessor;
	private int type;
	private TweenEquation equation;
	private TweenPath path;

	// General
	private boolean isFrom;
	private boolean isRelative;
	private int combinedAttrsCnt;
	private int waypointsCnt;

	// Values
	private final float[] startValues = new float[Tween.combinedAttrsLimit];
	private final float[] targetValues = new float[Tween.combinedAttrsLimit];
	private final float[] waypoints = new float[Tween.waypointsLimit * Tween.combinedAttrsLimit];

	// Buffers
	private float[] accessorBuffer = new float[Tween.combinedAttrsLimit];
	private float[] pathBuffer = new float[(2 + Tween.waypointsLimit) * Tween.combinedAttrsLimit];

	// -------------------------------------------------------------------------
	// Setup
	// -------------------------------------------------------------------------

	private Tween() {
		this.reset();
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
			this.accessorBuffer = new float[Tween.combinedAttrsLimit];
		}

		if (this.pathBuffer.length != (2 + Tween.waypointsLimit) * Tween.combinedAttrsLimit) {
			this.pathBuffer = new float[(2 + Tween.waypointsLimit) * Tween.combinedAttrsLimit];
		}
	}

	private void setup(final Object target, final int tweenType, final float duration) {
		if (duration < 0) {
			throw new RuntimeException("Duration can't be negative");
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

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public Tween ease(final TweenEquation easeEquation) {
		this.equation = easeEquation;
		return this;
	}

	public Tween cast(final Class<?> targetClass) {
		if (this.isStarted()) {
			throw new RuntimeException("You can't cast the target of a tween once it is started");
		}

		this.targetClass = targetClass;
		return this;
	}

	public Tween target(final float targetValue) {
		this.targetValues[0] = targetValue;
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

	public Tween target(final float... targetValues) {
		if (targetValues.length > Tween.combinedAttrsLimit) {
			this.throwCombinedAttrsLimitReached();
		}

		System.arraycopy(targetValues, 0, this.targetValues, 0, targetValues.length);
		return this;
	}

	public Tween targetRelative(final float targetValue) {
		this.isRelative = true;
		this.targetValues[0] = this.isInitialized() ? targetValue + this.startValues[0] : targetValue;
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

	public Tween targetRelative(final float... targetValues) {
		if (targetValues.length > Tween.combinedAttrsLimit) {
			this.throwCombinedAttrsLimitReached();
		}

		for (int i = 0; i < targetValues.length; i++) {
			this.targetValues[i] = this.isInitialized() ? targetValues[i] + this.startValues[i] : targetValues[i];
		}

		this.isRelative = true;
		return this;
	}

	public Tween waypoint(final float targetValue) {
		if (this.waypointsCnt == Tween.waypointsLimit) {
			this.throwWaypointsLimitReached();
		}

		this.waypoints[this.waypointsCnt] = targetValue;
		this.waypointsCnt += 1;
		return this;
	}

	public Tween waypoint(final float targetValue1, final float targetValue2) {
		if (this.waypointsCnt == Tween.waypointsLimit) {
			this.throwWaypointsLimitReached();
		}

		this.waypoints[this.waypointsCnt * 2] = targetValue1;
		this.waypoints[this.waypointsCnt * 2 + 1] = targetValue2;
		this.waypointsCnt += 1;
		return this;
	}

	public Tween waypoint(final float targetValue1, final float targetValue2, final float targetValue3) {
		if (this.waypointsCnt == Tween.waypointsLimit) {
			this.throwWaypointsLimitReached();
		}

		this.waypoints[this.waypointsCnt * 3] = targetValue1;
		this.waypoints[this.waypointsCnt * 3 + 1] = targetValue2;
		this.waypoints[this.waypointsCnt * 3 + 2] = targetValue3;
		this.waypointsCnt += 1;
		return this;
	}

	public Tween waypoint(final float... targetValues) {
		if (this.waypointsCnt == Tween.waypointsLimit) {
			this.throwWaypointsLimitReached();
		}

		System.arraycopy(targetValues, 0, this.waypoints, this.waypointsCnt * targetValues.length, targetValues.length);
		this.waypointsCnt += 1;
		return this;
	}

	public Tween path(final TweenPath path) {
		this.path = path;
		return this;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	public Object getTarget() {
		return this.target;
	}

	public int getType() {
		return this.type;
	}

	public TweenEquation getEasing() {
		return this.equation;
	}

	public float[] getTargetValues() {
		return this.targetValues;
	}

	public int getCombinedAttributesCount() {
		return this.combinedAttrsCnt;
	}

	public TweenAccessor<?> getAccessor() {
		return this.accessor;
	}

	public Class<?> getTargetClass() {
		return this.targetClass;
	}

	// -------------------------------------------------------------------------
	// Overrides
	// -------------------------------------------------------------------------

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
			throw new RuntimeException("No TweenAccessor was found for the target");
		}

		if (this.combinedAttrsCnt > Tween.combinedAttrsLimit) {
			this.throwCombinedAttrsLimitReached();
		}

		return this;
	}

	@Override
	public void free() {
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

		if (this.duration < 0.00000000001f && delta > -0.00000000001f) {
			this.accessor.setValues(this.target, this.type, this.isReverse(step) ? this.targetValues : this.startValues);
			return;
		}

		if (this.duration < 0.00000000001f && delta < 0.00000000001f) {
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

	// -------------------------------------------------------------------------
	// BaseTween impl.
	// -------------------------------------------------------------------------

	@Override
	protected void forceStartValues() {
		if (this.target == null) {
			return;
		}

		this.accessor.setValues(this.target, this.type, this.startValues);
	}

	@Override
	protected void forceEndValues() {
		if (this.target == null) {
			return;
		}

		this.accessor.setValues(this.target, this.type, this.targetValues);
	}

	@Override
	protected boolean containsTarget(final Object target) {
		return this.target == target;
	}

	@Override
	protected boolean containsTarget(final Object target, final int tweenType) {
		return this.target == target && this.type == tweenType;
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void throwCombinedAttrsLimitReached() {
		final String msg = "You cannot combine more than " + Tween.combinedAttrsLimit + " "
				+ "attributes in a tween. You can raise this limit with "
				+ "Tween.setCombinedAttributesLimit(), which should be called once "
				+ "in application initialization code.";
		throw new RuntimeException(msg);
	}

	private void throwWaypointsLimitReached() {
		final String msg = "You cannot add more than " + Tween.waypointsLimit + " "
				+ "waypoints to a tween. You can raise this limit with "
				+ "Tween.setWaypointsLimit(), which should be called once in "
				+ "application initialization code.";
		throw new RuntimeException(msg);
	}

}