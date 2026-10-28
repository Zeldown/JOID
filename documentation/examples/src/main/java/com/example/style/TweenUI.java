package com.example.style;

import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenManager;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TweenUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	// #region fields
	private final TweenManager manager = new TweenManager();

	private float x;
	private float y;
	// #endregion

	@Override
	public void init() {
		// #region timeline
		this.manager.killAll();
		this.x = 0F;
		this.y = 0F;

		Timeline
		.sequence()
		.push(Tween.to(() -> this.x, value -> this.x = value, 800F).target(1F).easing(TweenEquations.QUAD_IN_OUT))
		.pushPause(200F)
		.push(Tween.to(() -> this.y, value -> this.y = value, 600F).target(1F).easing(TweenEquations.BACK_OUT))
		.repeatYoyo(BaseTween.INFINITY, 400F)
		.start(this.manager);
		// #endregion

		// #region node
		RectNode
		.create(660, 340, 600, 400)
		.color(TweenUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TweenUI.ELEMENT, 1.5F))
		.body(area -> {
			RectNode.create(0, 0, 80, 80).color(TweenUI.ACCENT).x(() -> 520D * this.x).y(() -> 320D * this.y).attach(area);
		})
		.attach(this);
		// #endregion
	}

	// #region update
	@Override
	public void update() {
		this.manager.update((float) super.getFrameTime());
	}
	// #endregion

}