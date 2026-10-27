package com.example.describing.media;

import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class GalleryUI extends UI {

	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region image
		final Resource image = Resource.of("images/placeholder.png");
		// #endregion

		RectNode
		.create(430, 300, 1060, 480)
		.color(GalleryUI.PLACEHOLDER)
		.body(card -> {
			// #region natural
			ResourceNode.create(40, 40).resource(image).attach(card);
			ResourceNode.create(400, 40, 160, 0).resource(image).attach(card);
			// #endregion
			// #region tint
			ResourceNode.create(600, 40).resource(image).color(GalleryUI.DARK).hoveredColor(Color.WHITE).attach(card);
			// #endregion
			RectNode.create(40, 260, 180, 180).color(Color.WHITE).attach(card);
			RectNode.create(260, 260, 180, 180).color(Color.WHITE).attach(card);
			RectNode.create(480, 260, 180, 180).color(Color.WHITE).attach(card);
			// #region stretch
			ResourceNode.create(40, 260, 180, 180).resource(image).stretch(StretchType.STRETCH).attach(card);
			ResourceNode.create(260, 260, 180, 180).resource(image).stretch(StretchType.CONTAIN).attach(card);
			ResourceNode.create(480, 260, 180, 180).resource(image).stretch(StretchType.COVER).attach(card);
			// #endregion
			// #region crop
			final Resource corner = Resource.of("images/placeholder.png").textureCoords(0, 0, 160, 90);
			ResourceNode.create(700, 260, 320, 180).resource(corner).attach(card);
			// #endregion
		})
		.attach(this);
	}

}