package com.example.describing.flex;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class SettingsListUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 30F).color(SettingsListUI.INK);
		final String[] names = {"Sound", "Music", "Subtitles"};

		RectNode
		.create(660, 340, 600, 400)
		.color(SettingsListUI.PLACEHOLDER)
		.body(card -> {
			// #region list
			FlexNode
			.vertical(40, 40, 520)
			.margin(20D)
			.body(column -> {
				for (final String name : names) {
					FlexNode
					.horizontal(0, 0, 90)
					.margin(24D)
					.align(Align.CENTER)
					.body(row -> {
						CircleNode.create(0, 0, 50).color(Color.WHITE).attach(row);
						TextNode.create(0, 0).text(Text.create(name, info)).attach(row);
					})
					.attach(column);
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}