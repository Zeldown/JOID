package com.example.style;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.utils.align.Align;

public class PulseUI extends UI {

	private static final Color ACCENT = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region pulse
		final TweenAnimator pulse = TweenAnimator
		.create(1F)
		.sequence(600F, 1.2F, TweenEquations.SINE_IN_OUT)
		.repeatYoyo(BaseTween.INFINITY, 0F)
		.start();

		CircleNode
		.create(200, 200, 100)
		.color(PulseUI.ACCENT)
		.anchor(Align.CENTER)
		.width(100D * pulse.get())
		.height(100D * pulse.get())
		.onDetach(event -> pulse.clear())
		.attach(this);
		// #endregion
	}

}