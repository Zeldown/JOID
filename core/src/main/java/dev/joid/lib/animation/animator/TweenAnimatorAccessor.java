package dev.joid.lib.animation.animator;

import dev.joid.lib.animation.tween.TweenAccessor;

public class TweenAnimatorAccessor implements TweenAccessor<TweenAnimator> {

	public static final int ANIMATION_VALUE = 0;

	@Override
	public int getValues(final TweenAnimator target, final int tweenType, final float[] returnValues) {
		returnValues[0] = target.peek();
		return 1;
	}

	@Override
	public void setValues(final TweenAnimator target, final int tweenType, final float[] newValues) {
		target.writeValue(newValues[0]);
	}

}