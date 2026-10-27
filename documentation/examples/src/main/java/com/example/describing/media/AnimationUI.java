package com.example.describing.media;

import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class AnimationUI extends UI {

	@Override
	public void init() {
		RectNode
		.create(760, 410, 400, 260)
		.color(Color.WHITE)
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