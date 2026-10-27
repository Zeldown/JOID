package com.example.tutorial.state;

import java.util.Random;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ChallengeUI extends UI {

	private final Random              random = new Random(7L);
	private final ListSignal<Integer> rounds = new ListSignal<>();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(Theme.FONT, 36F).color(Theme.INK);

		// #region solution
		Shortcut.create(Key.P).onTrigger(event -> this.rounds.add(this.random.nextInt(900) + 100)).attach(this);
		// #endregion

		TextNode.create(160, 160).text(Text.create("Games: " + this.rounds.size(), caption)).attach(this);
	}

}