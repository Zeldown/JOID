package com.example.reference.animation;

import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenManager;

public class FadeTween {

	private final TweenManager manager = new TweenManager();
	private float alpha;

	public void fadeIn() {
		// #region tween
		Tween
		.to(() -> this.alpha, value -> this.alpha = value, 400F)
		.target(1F)
		.easing(TweenEquations.QUAD_OUT)
		.start(this.manager);
		// #endregion
	}

}