package be.zeldown.joid.lib.animation.tweenengine;

public abstract class TweenEquation {

	public abstract float compute(final float t);

	public boolean isValueOf(final String str) {
		return str.equals(this.toString());
	}

}