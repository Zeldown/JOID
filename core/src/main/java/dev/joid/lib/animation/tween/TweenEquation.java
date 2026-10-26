package dev.joid.lib.animation.tween;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TweenEquation {

	public abstract float compute(final float t);

	public boolean isValueOf(final @NonNull String name) {
		return name.equals(this.toString());
	}

}