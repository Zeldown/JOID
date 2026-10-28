package com.example.tutorial.interaction;

import dev.joid.lib.JOID;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.utils.align.Align;

public class MenuUI extends UI {

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.FONT, 120F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final TextInfo tagline = TextInfo.create(Theme.FONT, 36F).color(Theme.TEXT);
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Theme.TEXT);
		final TextInfo heading = TextInfo.create(Theme.FONT, 40F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final TextInfo caption = TextInfo.create(Theme.FONT, 26F).color(Theme.TEXT);
		final TextInfo value = TextInfo.create(Theme.FONT, 56F).weight(FontWeight.BOLD).color(Theme.TEXT);

		// #region shortcut
		Shortcut.create(Key.S).onTrigger(event -> JOID.open(new SettingsUI())).attach(this);
		// #endregion

		TextNode.create(160, 160).text(Text.create("Starfall", title)).attach(this);

		TextNode.create(164, 310).text(Text.create("Catch the falling stars", tagline)).attach(this);

		// #region buttons
		FlexNode
		.vertical(160, 420, 360)
		.margin(20D)
		.body(flex -> {
			RectNode
			.create(0, 0, 360, 80)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.label(Text.create("Play", label))
			.attach(flex);
			RectNode
			.create(0, 0, 360, 80)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.onClick(event -> JOID.open(new SettingsUI()))
			.label(Text.create("Settings", label))
			.attach(flex);
			RectNode
			.create(0, 0, 360, 80)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.onClick(event -> JOID.close(this))
			.label(Text.create("Quit", label))
			.attach(flex);
		})
		.attach(this);
		// #endregion

		// #region panel
		RectNode
		.create(760, 160, 1000, 760)
		.color(Theme.SURFACE)
		.body(panel -> {
			TextNode.create(48, 40).text(Text.create("Profile", heading)).attach(panel);
			// #region grid
			GridNode
			.create(48, 120, 904, 150)
			.margin(32D)
			.body(grid -> {
				RectNode
				.create(0, 0, 280, 150)
				.color(Theme.ELEMENT)
				.hover(() -> "Rounds you played")
				.body(card -> {
					TextNode.create(24, 24).text(Text.create("Games", caption)).attach(card);
					TextNode.create(24, 64).text(Text.create("3", value)).attach(card);
				})
				.attach(grid);
				RectNode
				.create(0, 0, 280, 150)
				.color(Theme.ELEMENT)
				.hover(() -> "Your highest score")
				.body(card -> {
					TextNode.create(24, 24).text(Text.create("Best", caption)).attach(card);
					TextNode.create(24, 64).text(Text.create("640", value)).attach(card);
				})
				.attach(grid);
				RectNode
				.create(0, 0, 280, 150)
				.color(Theme.ELEMENT)
				.hover(() -> "Every point you scored")
				.body(card -> {
					TextNode.create(24, 24).text(Text.create("Total", caption)).attach(card);
					TextNode.create(24, 64).text(Text.create("1370", value)).attach(card);
				})
				.attach(grid);
			})
			.attach(panel);
			// #endregion
			TextNode.create(48, 320).text(Text.create("Recent rounds", heading)).attach(panel);
			// #region rounds
			FlexNode
			.vertical(48, 390, 904)
			.margin(12D)
			.body(flex -> {
				final String[][] rounds = {{"Round 3", "412"}, {"Round 2", "640"}, {"Round 1", "318"}};
				for (final String[] round : rounds) {
					RectNode
					.create(0, 0, 904, 72)
					.color(Theme.ELEMENT)
					.body(row -> {
						TextNode
						.create(24, row.dh(2))
						.text(Text.create(round[0], caption))
						.anchorY(Align.CENTER)
						.attach(row);
						TextNode
						.create(row.aw(-24), row.dh(2))
						.text(Text.create(round[1], caption))
						.anchorX(Align.END)
						.anchorY(Align.CENTER)
						.attach(row);
					})
					.attach(flex);
				}
			})
			.attach(panel);
			// #endregion
		})
		.attach(this);
		// #endregion
	}

}