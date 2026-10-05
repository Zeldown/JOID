package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenAccessor<T> {

	public int getValues(final T target, final int tweenType, final float[] returnValues);

	public void setValues(final T target, final int tweenType, final float[] newValues);

}