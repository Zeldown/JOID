package com.example.describing.layout;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class GroupUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(660, 390, 600, 300)
		.color(GroupUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(GroupUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region group
			for (int i = 0; i < 2; i++) {
				ContainerNode
				.create(40 + i * 60, 40 + i * 120, 400, 100)
				.body(group -> {
					CircleNode.create(0, 0, 100).color(GroupUI.ELEMENT).attach(group);
					RectNode.create(130, 15, 270, 30).color(GroupUI.ACCENT).attach(group);
					RectNode.create(130, 60, 180, 25).color(GroupUI.ELEMENT).attach(group);
				})
				.attach(card);
			}
			// #endregion
		})
		.attach(this);
	}

}