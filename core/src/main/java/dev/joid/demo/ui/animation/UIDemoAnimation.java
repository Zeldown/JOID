package dev.joid.demo.ui.animation;

import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class UIDemoAnimation extends UIDemo {

	@Override
	public void init() {
		final TweenAnimator moveAnimator = TweenAnimator.create(0F).sequence(1000F, 1F);
		moveAnimator.getTimeline().repeatYoyo(-1, 0F);
		moveAnimator.start();

		RectNode
		.create(0, 0, 100, 100)
		.color(Color.RED)
		.onAnimate((node, animator, value) -> node.position((1920 - 100) * value, (1080 - 100) * value))
		.animate(moveAnimator)
		.attach(this);
	}

}