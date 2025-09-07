package be.zeldown.joid.demo.ui.animation;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.animation.animator.TweenAnimator;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;

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