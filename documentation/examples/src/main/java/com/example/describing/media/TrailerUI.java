package com.example.describing.media;

import java.io.File;

import com.example.ExampleFont;

import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TrailerUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		RectNode
		.create(600, 230, 720, 580)
		.color(TrailerUI.PLACEHOLDER)
		.body(card -> {
			// #region video
			final ResourcePlayerNode trailer = ResourcePlayerNode
			.create(40, 40, 640, 360)
			.resource(Resource.of(new File("videos/trailer.mp4")))
			.autoplay(false)
			.volume(0.5F)
			.onEnd(event -> JOID.close(this))
			.attach(card);
			// #endregion
			// #region controls
			RectNode
			.create(40, 440, 140, 50)
			.color(TrailerUI.INK)
			.onClick(event -> trailer.toggle())
			.label(Text.create("Play", label))
			.attach(card);
			RectNode
			.create(200, 440, 140, 50)
			.color(TrailerUI.INK)
			.onClick(event -> trailer.seek(30D))
			.label(Text.create("0:30", label))
			.attach(card);
			RectNode.create(360, 460, 320, 10).color(Color.WHITE).attach(card);
			RectNode.create(360, 460, 0, 10).color(TrailerUI.DARK).width(() -> trailer.getProgress() * 320D).attach(card);
			// #endregion
		})
		.attach(this);
	}

}