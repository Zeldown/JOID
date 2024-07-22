package be.zeldown.joid.lib.animation.tweenengine;

public abstract class TweenEquation {

	public abstract float compute(float t);

	public boolean isValueOf(final String str) {
		return str.equals(this.toString());
	}

}