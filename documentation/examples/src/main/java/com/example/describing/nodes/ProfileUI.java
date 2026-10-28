package com.example.describing.nodes;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ProfileUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region body
		RectNode
		.create(660, 290, 600, 500)
		.color(ProfileUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ProfileUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode.create(40, 40, 520, 80).color(ProfileUI.ACCENT).attach(card);
			RectNode
			.create(40, 150, 520, 310)
			.color(ProfileUI.ELEMENT)
			.body(content -> {
				CircleNode.create(30, 30, 120).color(ProfileUI.MUTED).attach(content);
				RectNode.create(180, 50, 300, 30).color(ProfileUI.MUTED).attach(content);
				RectNode.create(180, 100, 200, 30).color(ProfileUI.MUTED).attach(content);
			})
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}