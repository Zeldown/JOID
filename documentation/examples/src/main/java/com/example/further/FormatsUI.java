package com.example.further;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.locator.AssetLocator;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.format.ResourceFormat;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;

public class FormatsUI extends UI {

	static {
		// #region register
		AssetLocator.register(new FormatsPackAssetLocator());
		ResourceFormat.register(new FormatsPpmResourceFormat());
		// #endregion
	}

	// #region strip
	private static final Resource COIN = ResourceBuilder
	.create()
	.nearest()
	.compute("strip:coin", () -> ResourceData.create(
		"strip:coin",
		AnimatedResourceDecoder.create(Asset.of("pack:images/strip.png"), new FormatsStripAnimationReader(8, 80L))
	));
	// #endregion

	@Override
	public void init() {
		// #region ppm
		ResourceNode.create(100, 100, 192, 192).resource(Resource.of("pack:images/ring.ppm").nearest()).attach(this);
		// #endregion

		// #region coin
		ResourceNode.create(360, 100, 192, 192).resource(FormatsUI.COIN).attach(this);
		// #endregion
	}

}