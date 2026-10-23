package dev.joid.demo.replay;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@UIDataDebug(profiler = false, hotreload = false)
public class ReplayAnimatorUI extends UI {

	private final TweenAnimator widthAnimator;
	private final TweenAnimator heightAnimator;

	@Override
	public void init() {
		this.widthAnimator.start();
		this.heightAnimator.start();
		RectNode.create(0D, 0D, 10D, 10D).width(10D + 90D * this.widthAnimator.get()).height(10D + 90D * this.heightAnimator.get()).attach(this);
	}

}