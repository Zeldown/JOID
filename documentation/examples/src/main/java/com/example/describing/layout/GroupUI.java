package com.example.describing.layout;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class GroupUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	@Override
	public void init() {
		RectNode
		.create(660, 390, 600, 300)
		.color(GroupUI.PLACEHOLDER)
		.body(card -> {
			// #region group
			for (int i = 0; i < 2; i++) {
				ContainerNode
				.create(40 + i * 60, 40 + i * 120, 400, 100)
				.body(group -> {
					CircleNode.create(0, 0, 100).color(Color.WHITE).attach(group);
					RectNode.create(130, 15, 270, 30).color(GroupUI.INK).attach(group);
					RectNode.create(130, 60, 180, 25).color(Color.WHITE).attach(group);
				})
				.attach(card);
			}
			// #endregion
		})
		.attach(this);
	}

}