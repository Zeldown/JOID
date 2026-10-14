package dev.joid.lib.animation.tween;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class TweenValue {

	private float value;

	public Tween to(final float duration) {
		return Tween.to(this::getValue, this::setValue, duration);
	}

}