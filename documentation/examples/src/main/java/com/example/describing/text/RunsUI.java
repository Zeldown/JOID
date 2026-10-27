package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextElement;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class RunsUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	private int clicks;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(RunsUI.INK);
		final TextInfo bold = info.withWeight(FontWeight.BOLD).withColor(RunsUI.DARK);

		RectNode
		.create(610, 340, 700, 400)
		.color(RunsUI.PLACEHOLDER)
		.body(card -> {
			// #region runs
			TextNode
			.create(50, 50)
			.text(Text.create(TextElement.create("Welcome back, ", info), TextElement.create("Ada", bold)))
			.attach(card);
			// #endregion
			// #region supplier
			RectNode
			.create(50, 140, 400, 80)
			.color(Color.WHITE)
			.onClick(event -> this.clicks++)
			.body(button -> {
				TextNode
				.create(0, 0, 400, 80)
				.text(Text.create(() -> "Clicks: " + this.clicks, info).align(Align.CENTER))
				.attach(button);
			})
			.attach(card);
			// #endregion
			// #region measure
			final String label = "Settings";
			RectNode
			.create(50, 270, info.aw(label, 48D), info.ah(24D))
			.color(Color.WHITE)
			.body(box -> TextNode.create(24, 12).text(Text.create(label, info)).attach(box))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}