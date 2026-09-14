package be.zeldown.joid.lib.animation.tweenengine;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Timeline extends BaseTween<Timeline> {

	// -------------------------------------------------------------------------
	// Static -- pool
	// -------------------------------------------------------------------------

	private static final Pool.Callback<Timeline> poolCallback = new Pool.Callback<Timeline>() {

		@Override
		public void onPool(final Timeline obj) {
			obj.reset();
		}

		@Override
		public void onUnPool(final Timeline obj) {
			obj.reset();
		}

	};

	static final Pool<Timeline> pool = new Pool<Timeline>(10, Timeline.poolCallback) {

		@Override
		protected Timeline create() {
			return new Timeline();
		}

	};

	public static int getPoolSize() {
		return Timeline.pool.size();
	}

	public static void ensurePoolCapacity(final int minCapacity) {
		Timeline.pool.ensureCapacity(minCapacity);
	}

	// -------------------------------------------------------------------------
	// Static -- factories
	// -------------------------------------------------------------------------

	public static Timeline createSequence() {
		final Timeline tl = Timeline.pool.get();
		tl.setup(Modes.SEQUENCE);
		return tl;
	}

	public static Timeline createParallel() {
		final Timeline tl = Timeline.pool.get();
		tl.setup(Modes.PARALLEL);
		return tl;
	}

	// -------------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------------

	private enum Modes {
		SEQUENCE,
		PARALLEL
	}

	private final List<BaseTween<?>> children = new ArrayList<>(10);
	private Timeline current;
	private Timeline parent;
	private Modes mode;
	private boolean isBuilt;

	// -------------------------------------------------------------------------
	// Setup
	// -------------------------------------------------------------------------

	private Timeline() {
		this.reset();
	}

	@Override
	protected void reset() {
		super.reset();

		this.children.clear();
		this.current = this.parent = null;

		this.isBuilt = false;
	}

	private void setup(final Modes mode) {
		this.mode = mode;
		this.current = this;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public Timeline push(final Tween tween) {
		if (this.isBuilt) {
			throw new RuntimeException("You can't push anything to a timeline once it is started");
		}

		this.current.children.add(tween);
		return this;
	}

	public Timeline push(final Timeline timeline) {
		if (this.isBuilt) {
			throw new RuntimeException("You can't push anything to a timeline once it is started");
		}

		if (timeline.current != timeline) {
			throw new RuntimeException("You forgot to call a few 'end()' statements in your pushed timeline");
		}

		timeline.parent = this.current;
		this.current.children.add(timeline);
		return this;
	}

	public Timeline pushPause(final float time) {
		if (this.isBuilt) {
			throw new RuntimeException("You can't push anything to a timeline once it is started");
		}

		this.current.children.add(Tween.mark().delay(time));
		return this;
	}

	public Timeline beginSequence() {
		if (this.isBuilt) {
			throw new RuntimeException("You can't push anything to a timeline once it is started");
		}

		final Timeline tl = Timeline.pool.get();
		tl.parent = this.current;
		tl.mode = Modes.SEQUENCE;
		this.current.children.add(tl);
		this.current = tl;
		return this;
	}

	public Timeline beginParallel() {
		if (this.isBuilt) {
			throw new RuntimeException("You can't push anything to a timeline once it is started");
		}

		final Timeline tl = Timeline.pool.get();
		tl.parent = this.current;
		tl.mode = Modes.PARALLEL;
		this.current.children.add(tl);
		this.current = tl;
		return this;
	}

	public Timeline end() {
		if (this.isBuilt) {
			throw new RuntimeException("You can't push anything to a timeline once it is started");
		}

		if (this.current == this) {
			throw new RuntimeException("Nothing to end...");
		}

		this.current = this.current.parent;
		return this;
	}

	public List<BaseTween<?>> getChildren() {
		if (this.isBuilt) {
			return Collections.unmodifiableList(this.current.children);
		} else {
			return this.current.children;
		}
	}

	// -------------------------------------------------------------------------
	// Overrides
	// -------------------------------------------------------------------------

	@Override
	public Timeline build() {
		if (this.isBuilt) {
			return this;
		}

		this.duration = 0;
		for (final BaseTween<?> element : this.children) {
			final BaseTween<?> obj = element;

			if (obj.getRepeatCount() < 0) {
				throw new RuntimeException("You can't push an object with infinite repetitions in a timeline");
			}

			obj.build();

			switch (this.mode) {
			case SEQUENCE:
				final float tDelay = this.duration;
				this.duration += obj.getFullDuration();
				obj.delay += tDelay;
				break;

			case PARALLEL:
				this.duration = Math.max(this.duration, obj.getFullDuration());
				break;
			}
		}

		this.isBuilt = true;
		return this;
	}

	@Override
	public Timeline start() {
		super.start();

		for (final BaseTween<?> obj : this.children) {
			obj.start();
		}

		return this;
	}

	@Override
	public void free() {
		for (int i = this.children.size() - 1; i >= 0; i--) {
			final BaseTween<?> obj = this.children.remove(i);
			obj.free();
		}

		Timeline.pool.free(this);
	}

	@Override
	protected void updateOverride(final int step, final int lastStep, final boolean isIterationStep, final float delta) {
		if (!isIterationStep && step > lastStep) {
			assert delta >= 0;
			final float dt = this.isReverse(lastStep) ? -delta - 1 : delta + 1;
			for (final BaseTween<?> element : this.children) {
				element.update(dt);
			}
			return;
		}

		if (!isIterationStep && step < lastStep) {
			assert delta <= 0;
			final float dt = this.isReverse(lastStep) ? -delta - 1 : delta + 1;
			for (int i = this.children.size() - 1; i >= 0; i--) {
				this.children.get(i).update(dt);
			}
			return;
		}

		assert isIterationStep;

		if (step > lastStep) {
			if (this.isReverse(step)) {
				this.forceEndValues();
				for (final BaseTween<?> element : this.children) {
					element.update(delta);
				}
			} else {
				this.forceStartValues();
				for (final BaseTween<?> element : this.children) {
					element.update(delta);
				}
			}
		} else if (step < lastStep) {
			if (this.isReverse(step)) {
				this.forceStartValues();
				for (int i = this.children.size() - 1; i >= 0; i--) {
					this.children.get(i).update(delta);
				}
			} else {
				this.forceEndValues();
				for (int i = this.children.size() - 1; i >= 0; i--) {
					this.children.get(i).update(delta);
				}
			}
		} else {
			final float dt = this.isReverse(step) ? -delta : delta;
			if (delta >= 0) {
				for (final BaseTween<?> element : this.children) {
					element.update(dt);
				}
			} else {
				for (int i = this.children.size() - 1; i >= 0; i--) {
					this.children.get(i).update(dt);
				}
			}
		}
	}

	// -------------------------------------------------------------------------
	// BaseTween impl.
	// -------------------------------------------------------------------------

	@Override
	protected void forceStartValues() {
		for (int i = this.children.size() - 1; i >= 0; i--) {
			final BaseTween<?> obj = this.children.get(i);
			obj.forceToStart();
		}
	}

	@Override
	protected void forceEndValues() {
		for (final BaseTween<?> obj : this.children) {
			obj.forceToEnd(this.duration);
		}
	}

	@Override
	protected boolean containsTarget(final Object target) {
		for (final BaseTween<?> obj : this.children) {
			if (obj.containsTarget(target)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean containsTarget(final Object target, final int tweenType) {
		for (final BaseTween<?> obj : this.children) {
			if (obj.containsTarget(target, tweenType)) {
				return true;
			}
		}
		return false;
	}

}