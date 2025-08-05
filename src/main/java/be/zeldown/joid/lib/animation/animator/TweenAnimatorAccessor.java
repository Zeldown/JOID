package be.zeldown.joid.lib.animation.animator;

import be.zeldown.joid.lib.animation.tweenengine.TweenAccessor;

public class TweenAnimatorAccessor implements TweenAccessor<TweenAnimator> {

	public static final int ANIMATION_VALUE = 0;

	@Override
	public int getValues(final TweenAnimator target, final int tweenType, final float[] returnValues) {
		switch (tweenType) {
		case ANIMATION_VALUE:
			returnValues[0] = target.getValue();
			break;

		default:
			break;
		}

		return 1;
	}

	@Override
	public void setValues(final TweenAnimator target, final int tweenType, final float[] newValues) {
		switch (tweenType) {
		case ANIMATION_VALUE:
			target.setValue(newValues[0]);
			break;

		default:
			break;
		}
	}

}