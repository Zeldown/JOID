package dev.joid.lib.animation.tween;

public interface TweenAccessor<T> {

	public int getValues(final T target, final int tweenType, final float[] returnValues);

	public void setValues(final T target, final int tweenType, final float[] newValues);

}