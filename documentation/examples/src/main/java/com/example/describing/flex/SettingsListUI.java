package com.example.describing.flex;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class SettingsListUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 30F).color(SettingsListUI.TEXT);
		final String[] names = {"Sound", "Music", "Subtitles"};

		RectNode
		.create(660, 340, 600, 400)
		.color(SettingsListUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(SettingsListUI.ELEMENT, 1.5F))
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
						CircleNode.create(0, 0, 50).color(SettingsListUI.ACCENT).attach(row);
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