package com.example.reference.animation;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region slide
@UIData(background = true, backgroundColor = "#DDDDDD")
public class SlideUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	private final TweenAnimator slide = TweenAnimator.create();

	@Override
	public void init() {
		RectNode
		.create(80, 80, 400, 60)
		.color(Color.WHITE)
		.onClick(event -> this.slide.sequence(600F, 1F, TweenEquations.QUAD_OUT).push(600F, 0F).start())
		.body(rect -> {
			RectNode.create(0, 0, 60, 60).color(SlideUI.INK).x(() -> 340D * this.slide.get()).attach(rect);
		})
		.attach(this);
	}

}
// #endregion