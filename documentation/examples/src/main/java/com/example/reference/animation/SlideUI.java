package com.example.reference.animation;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region slide
public class SlideUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private final TweenAnimator slide = TweenAnimator.create();

	@Override
	public void init() {
		RectNode
		.create(80, 80, 400, 60)
		.color(SlideUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(SlideUI.ELEMENT, 1.5F))
		.onClick(event -> this.slide.sequence(600F, 1F, TweenEquations.QUAD_OUT).push(600F, 0F).start())
		.body(rect -> {
			RectNode.create(10, 10, 40, 40).color(SlideUI.ACCENT).x(10D + 340D * this.slide.get()).attach(rect);
		})
		.attach(this);
	}

}
// #endregion