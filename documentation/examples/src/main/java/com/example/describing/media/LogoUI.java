package com.example.describing.media;

import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;

public class LogoUI extends UI {

	private static final Color MUTED = new Color(139, 147, 163).lock();

	@Override
	public void init() {
		// #region logo
		final Resource logo = Resource.of("images/placeholder.png");

		ResourceNode
		.create(100, 100)
		.resource(logo)
		.color(LogoUI.MUTED)
		.hoveredColor(Color.WHITE)
		.attach(this);
		// #endregion
	}

}