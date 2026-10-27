package com.example.describing.media;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;

public class PreloadUI extends UI {

	// #region preload
	private final Resource background = Resource.of("images/placeholder.png");

	public PreloadUI() {
		this.background.generate();
	}
	// #endregion

	@Override
	public void init() {
		ResourceNode.create(0, 0, 1920, 1080).resource(this.background).attach(this);
	}

}