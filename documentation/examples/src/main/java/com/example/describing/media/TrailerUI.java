package com.example.describing.media;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TrailerUI extends UI {

	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(TrailerUI.ON_ACCENT);

		RectNode
		.create(600, 230, 720, 580)
		.color(TrailerUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TrailerUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region video
			final ResourcePlayerNode trailer = ResourcePlayerNode
			.create(40, 40, 640, 360)
			.resource(Resource.of("videos/intro.mp4"))
			.autoplay(false)
			.volume(0.5F)
			.onEnd(event -> JOID.close(this))
			.attach(card);
			// #endregion
			// #region controls
			RectNode
			.create(40, 440, 140, 50)
			.color(TrailerUI.ACCENT)
			.onClick(event -> trailer.toggle())
			.label(Text.create("Play", label))
			.attach(card);
			RectNode
			.create(200, 440, 140, 50)
			.color(TrailerUI.ACCENT)
			.onClick(event -> trailer.seek(5D))
			.label(Text.create("0:05", label))
			.attach(card);
			RectNode.create(360, 460, 320, 10).color(TrailerUI.ELEMENT).attach(card);
			RectNode.create(360, 460, 0, 10).color(TrailerUI.ACCENT).width(() -> trailer.getProgress() * 320D).attach(card);
			// #endregion
		})
		.attach(this);
	}

}