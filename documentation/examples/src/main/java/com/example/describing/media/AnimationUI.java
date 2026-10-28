package com.example.describing.media;

import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class AnimationUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		RectNode
		.create(760, 410, 400, 260)
		.color(AnimationUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(AnimationUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region animation
			ResourcePlayerNode
			.create(40, 40)
			.resource(Resource.of("images/animation.gif"))
			.loop(true)
			.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}