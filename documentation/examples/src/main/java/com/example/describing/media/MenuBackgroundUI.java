package com.example.describing.media;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;

public class MenuBackgroundUI extends UI {

	@Override
	public void init() {
		// #region background
		ResourcePlayerNode
		.create(0, 0, 1920, 1080)
		.resource(Resource.of("videos/intro.mp4"))
		.loop(true)
		.volume(0F)
		.stretch(StretchType.COVER)
		.attach(this);
		// #endregion
	}

}