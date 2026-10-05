package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenAccessor<T> {

	public void setValues(final T target, final int tweenType, final float[] newValues);

	public int getValues(final T target, final int tweenType, final float[] returnValues);

}