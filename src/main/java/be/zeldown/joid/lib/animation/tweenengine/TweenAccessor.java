package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenAccessor<T> {

	int getValues(T target, int tweenType, float[] returnValues);
	void setValues(T target, int tweenType, float[] newValues);

}