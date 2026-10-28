package com.example.describing.media;

import java.io.File;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class LoadingUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(LoadingUI.TEXT);

		RectNode
		.create(410, 330, 1100, 420)
		.color(LoadingUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LoadingUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region events
			final TextNode status = TextNode.create(40, 250).text(Text.create("Loading...", info));
			final Resource photo = Resource
			.of(new File("photos/beach.png"))
			.onLoad(resource -> status.text(Text.create(resource.getWidth() + " x " + resource.getHeight(), info)))
			.onError((resource, error) -> status.text(Text.create("Missing photo", info)));
			ResourceNode.create(40, 40, 320, 180).resource(photo).attach(card);
			status.attach(card);
			// #endregion
			// #region blocking
			ResourceNode.create(400, 40).resource(Resource.of("images/placeholder.png").blocking()).attach(card);
			// #endregion
			// #region builder
			final ResourceBuilder pixelArt = ResourceBuilder.create().nearest().mipmap(false);
			ResourceNode.create(760, 40).resource(pixelArt.of("images/placeholder.png")).attach(card);
			// #endregion
		})
		.attach(this);
	}

}