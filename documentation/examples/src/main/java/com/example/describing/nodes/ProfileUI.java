package com.example.describing.nodes;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ProfileUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region body
		RectNode
		.create(660, 290, 600, 500)
		.color(ProfileUI.PLACEHOLDER)
		.body(card -> {
			RectNode.create(40, 40, 520, 80).color(ProfileUI.INK).attach(card);
			RectNode
			.create(40, 150, 520, 310)
			.color(Color.WHITE)
			.body(content -> {
				CircleNode.create(30, 30, 120).color(ProfileUI.PLACEHOLDER).attach(content);
				RectNode.create(180, 50, 300, 30).color(ProfileUI.PLACEHOLDER).attach(content);
				RectNode.create(180, 100, 200, 30).color(ProfileUI.PLACEHOLDER).attach(content);
			})
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}