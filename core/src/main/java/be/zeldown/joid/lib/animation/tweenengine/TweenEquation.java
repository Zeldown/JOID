package be.zeldown.joid.lib.animation.tweenengine;

public abstract class TweenEquation {

	public boolean isValueOf(final String str) {
		return str.equals(this.toString());
	}

	public abstract float compute(final float t);

}