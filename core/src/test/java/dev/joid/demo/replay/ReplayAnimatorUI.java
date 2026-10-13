package dev.joid.demo.replay;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReplayAnimatorUI extends UI {

	private final TweenAnimator animator;

	@Override
	public void init() {
		this.animator.start();
		RectNode.create(0D, 0D, 10D, 10D).width(10D + 90D * this.animator.get()).height(10D + 90D * this.animator.channel(1).get()).attach(this);
	}

}