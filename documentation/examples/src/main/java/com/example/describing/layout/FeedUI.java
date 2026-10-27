package com.example.describing.layout;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;

public class FeedUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(Color.WHITE);

		RectNode
		.create(720, 310, 480, 440)
		.color(FeedUI.PLACEHOLDER)
		.body(card -> {
			// #region scroll
			final RectNode feed = RectNode
			.create(20, 20, 440, 320)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				for (int i = 0; i < 8; i++) {
					RectNode.create(20, 20 + i * 80, 400, 60).color(FeedUI.INK).attach(area);
				}
			})
			.attach(card);
			// #endregion
			// #region more
			feed.onScrollEndReached(event -> {
				final Node area = event.getNode();
				final int count = area.getChildren().size();
				for (int i = count; i < count + 4; i++) {
					RectNode.create(20, 20 + i * 80, 400, 60).color(FeedUI.INK).attach(area);
				}
			});
			// #endregion
			// #region top
			RectNode
			.create(20, 360, 200, 60)
			.color(FeedUI.INK)
			.label(Text.create("Top", info))
			.onClick(event -> feed.scrollTo(0D, 0D))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}