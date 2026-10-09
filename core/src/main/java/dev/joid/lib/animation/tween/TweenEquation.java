package dev.joid.lib.animation.tween;

public abstract class TweenEquation {

	public abstract float compute(final float t);

	public boolean isValueOf(final String str) {
		return str.equals(this.toString());
	}

}