package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenAccessor<T> {

	public int getValues(T target, int tweenType, float[] returnValues);

	public void setValues(T target, int tweenType, float[] newValues);

}