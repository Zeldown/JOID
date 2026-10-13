package dev.joid.lib.animation.animator;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class TweenAnimatorChannel extends Signal<Float> {

	private float value;

	protected TweenAnimatorChannel(final float value) {
		super(value);
		this.value = value;
	}

	@Override
	public @NonNull TweenAnimatorChannel reset() {
		throw new UnsupportedOperationException("A TweenAnimatorChannel is read-only and takes the value of its animator: call setValue(index, value) on the animator instead");
	}

	@Override
	public @NonNull TweenAnimatorChannel set(final Float value) {
		throw new UnsupportedOperationException("A TweenAnimatorChannel is read-only and takes the value of its animator: call setValue(index, value) on the animator instead");
	}

	@Override
	public Float peek() {
		return this.value;
	}

	@Override
	public boolean isPresent() {
		return super.get() != null;
	}

	protected void update(final float value) {
		if (Float.compare(this.value, value) != 0) {
			this.value = value;
			super.publish();
		}
	}

}