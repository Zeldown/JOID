package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

public final class Timeline extends BaseTween<Timeline> {

	private final Mode               mode;
	private final List<BaseTween<?>> children = new ArrayList<>();

	private Timeline parent;
	private boolean  isBuilt;
	private Timeline current;

	private Timeline(final Mode mode) {
		this.mode    = mode;
		this.current = this;
	}

	public static @NonNull Timeline sequence() {
		return new Timeline(Mode.SEQUENCE);
	}

	public static @NonNull Timeline parallel() {
		return new Timeline(Mode.PARALLEL);
	}

	@Override
	public @NonNull Timeline start() {
		super.start();

		for (final BaseTween<?> child : this.children) {
			child.start();
		}

		return this;
	}

	@Override
	public @NonNull Timeline build() {
		if (this.isBuilt) {
			return this;
		}

		if (this.current != this) {
			throw new IllegalStateException("The timeline still has an open beginSequence() or beginParallel(), close each one with end() before start()");
		}

		super.duration = 0F;
		for (final BaseTween<?> child : this.children) {
			if (child.getRepeatCount() < 0) {
				throw new IllegalArgumentException("A timeline cannot hold a child that repeats forever (repeat count " + child.getRepeatCount() + "), give it a finite repeat count");
			}

			child.build();

			if (this.mode == Mode.SEQUENCE) {
				final float delay = super.duration;
				super.duration += child.getFullDuration();
				child.delay += delay;
			} else {
				super.duration = Math.max(super.duration, child.getFullDuration());
			}
		}

		this.isBuilt = true;
		return this;
	}

	@Override
	public @NonNull Timeline kill() {
		super.kill();

		for (final BaseTween<?> child : this.children) {
			child.kill();
		}

		return this;
	}

	public @NonNull Timeline push(final @NonNull Tween tween) {
		this.requireOpen();
		this.current.children.add(tween);
		return this;
	}

	public @NonNull Timeline push(final @NonNull Timeline timeline) {
		this.requireOpen();
		if (timeline.current != timeline) {
			throw new IllegalArgumentException("The pushed timeline still has an open beginSequence() or beginParallel(), close each one with end() before pushing it");
		}

		this.current.children.add(timeline);
		return this;
	}

	public @NonNull Timeline pushPause(final float time) {
		this.requireOpen();
		this.current.children.add(Tween.mark().delay(time));
		return this;
	}

	public @NonNull Timeline beginSequence() {
		return this.begin(Mode.SEQUENCE);
	}

	public @NonNull Timeline beginParallel() {
		return this.begin(Mode.PARALLEL);
	}

	public @NonNull Timeline end() {
		this.requireOpen();
		if (this.current == this) {
			throw new IllegalStateException("The timeline has no open beginSequence() or beginParallel() to close, remove this end()");
		}

		this.current = this.current.parent;
		return this;
	}

	public @NonNull List<BaseTween<?>> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	@Override
	protected void updateValues(final int step, final int lastStep, final boolean isIterationStep, final float delta) {
		if (!isIterationStep && step > lastStep) {
			this.updateForward(super.isReverse(lastStep) ? -delta - 1F : delta + 1F);
			return;
		}

		if (!isIterationStep && step < lastStep) {
			this.updateBackward(super.isReverse(lastStep) ? -delta + 1F : delta - 1F);
			return;
		}

		if (step > lastStep) {
			if (super.isReverse(step)) {
				this.forceEndValues();
			} else {
				this.forceStartValues();
			}
			this.updateForward(0F);
		} else if (step < lastStep) {
			if (super.isReverse(step)) {
				this.forceStartValues();
			} else {
				this.forceEndValues();
			}
			this.updateBackward(0F);
		} else if (delta >= 0F) {
			this.updateForward(super.isReverse(step) ? -delta : delta);
		} else {
			this.updateBackward(super.isReverse(step) ? -delta : delta);
		}
	}

	@Override
	protected void forceStartValues() {
		for (int i = this.children.size() - 1; i >= 0; i--) {
			this.children.get(i).forceToStart();
		}
	}

	@Override
	protected void forceEndValues() {
		for (final BaseTween<?> child : this.children) {
			child.forceToEnd(super.duration);
		}
	}

	private Timeline begin(final Mode mode) {
		this.requireOpen();
		final Timeline timeline = new Timeline(mode);
		timeline.parent = this.current;
		this.current.children.add(timeline);
		this.current = timeline;
		return this;
	}

	private void requireOpen() {
		if (this.isBuilt) {
			throw new IllegalStateException("A timeline takes no new child once it is built (start() builds it), push its children before start()");
		}
	}

	private void updateForward(final float delta) {
		for (final BaseTween<?> child : this.children) {
			child.update(delta);
		}
	}

	private void updateBackward(final float delta) {
		for (int i = this.children.size() - 1; i >= 0; i--) {
			this.children.get(i).update(delta);
		}
	}

	private enum Mode {
		SEQUENCE,
		PARALLEL;
	}

}