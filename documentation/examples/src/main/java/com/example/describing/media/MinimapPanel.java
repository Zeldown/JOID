package com.example.describing.media;

import java.util.function.IntSupplier;

import dev.joid.base.opengl.render.texture.GlBorrowedTexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;

public final class MinimapPanel {

	private MinimapPanel() {}

	public static ResourceNode create(final IntSupplier minimapTexture) {
		// #region texture
		final Resource minimap = Resource.of(GlBorrowedTexture.of(minimapTexture));
		return ResourceNode.create(1500, 60, 360, 360).resource(minimap);
		// #endregion
	}

}