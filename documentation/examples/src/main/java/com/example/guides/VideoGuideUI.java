package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class VideoGuideUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(VideoGuideUI.INK);

		// #region player
		final ResourcePlayerNode player = ResourcePlayerNode
		.create(560, 200, 800, 450)
		.resource(Resource.of("/videos/intro.mp4"))
		.stretch(StretchType.CONTAIN)
		.loop(true)
		.volume(0.8F)
		.attach(this);
		// #endregion

		// #region progress
		RectNode
		.create(560, 670, 800, 8)
		.color(Color.WHITE)
		.body(bar -> {
			RectNode
			.create(0, 0, 0, 8)
			.color(VideoGuideUI.INK)
			.width(() -> 800D * player.getProgress())
			.attach(bar);
		})
		.attach(this);
		// #endregion

		// #region button
		RectNode
		.create(860, 710, 200, 70)
		.color(Color.WHITE)
		.label(() -> Text.create(player.isPlaying() ? "Pause" : "Play", info))
		.onClick(event -> player.toggle())
		.attach(this);
		// #endregion
	}

}