package com.example.tutorial.settings;

import java.util.List;
import java.util.Random;

import dev.joid.lib.JOID;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.utils.align.Align;

public class MenuUI extends UI {

	// #region fields
	private final Random                  random = new Random(7L);
	private final ListSignal<Integer>     rounds = new ListSignal<>();
	private final ComputedSignal<Integer> best   = this.rounds.map(list -> list.stream().reduce(0, Math::max));
	private final ComputedSignal<Integer> total  = this.rounds.map(list -> list.stream().reduce(0, Integer::sum));
	// #endregion

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.FONT, 120F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final TextInfo tagline = TextInfo.create(Theme.FONT, 36F).color(Theme.TEXT);
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Theme.TEXT);
		final TextInfo heading = TextInfo.create(Theme.FONT, 40F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final TextInfo caption = TextInfo.create(Theme.FONT, 26F).color(Theme.TEXT);
		final TextInfo value = TextInfo.create(Theme.FONT, 56F).weight(FontWeight.BOLD).color(Theme.TEXT);
		// #region store
		final TextInfo status = TextInfo.create(Theme.FONT, 32F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final SettingsStore settings = super.useStore(SettingsStore.class, SettingsStore::new);
		// #endregion

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
			// #region play
			RectNode
			.create(0, 0, 360, 80)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.onClick(event -> this.rounds.add(this.random.nextInt(900) + 100))
			.label(Text.create("Play", label))
			.attach(flex);
			// #endregion
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

		// #region footer
		final BooleanSignal music = settings.getMusic();
		final IntegerSignal volume = settings.getVolume();
		TextNode
		.create(164, 760)
		.text(Text.create((music.get() ? "Music on" : "Music off") + ", volume " + volume.get() * 10 + " %", status))
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
					TextNode.create(24, 64).text(Text.create(String.valueOf(this.rounds.size()), value)).attach(card);
				})
				.attach(grid);
				RectNode
				.create(0, 0, 280, 150)
				.color(Theme.ELEMENT)
				.hover(() -> "Your highest score")
				.body(card -> {
					TextNode.create(24, 24).text(Text.create("Best", caption)).attach(card);
					TextNode.create(24, 64).text(Text.create(String.valueOf(this.best.get()), value)).attach(card);
				})
				.attach(grid);
				RectNode
				.create(0, 0, 280, 150)
				.color(Theme.ELEMENT)
				.hover(() -> "Every point you scored")
				.body(card -> {
					TextNode.create(24, 24).text(Text.create("Total", caption)).attach(card);
					TextNode.create(24, 64).text(Text.create(String.valueOf(this.total.get()), value)).attach(card);
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
			.watch(this.rounds, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
			.body(flex -> {
				final List<Integer> list = this.rounds.peek();
				if (list.isEmpty()) {
					TextNode.create(0, 0).text(Text.create("Press Play to start a round", caption)).attach(flex);
				}

				for (int index = list.size() - 1; index >= Math.max(0, list.size() - 4); index--) {
					final String[] round = {"Round " + (index + 1), String.valueOf(list.get(index))};
					RectNode
					.create(0, 0, 904, 72)
					.color(Theme.ELEMENT)
					.body(row -> {
						TextNode
						.create(24, row.dh(2D))
						.text(Text.create(round[0], caption))
						.anchorY(Align.CENTER)
						.attach(row);
						TextNode
						.create(row.aw(-24D), row.dh(2D))
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