package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class CustomNodesUI extends UI {

	private static final Color TEXT   = new Color(231, 233, 238).lock();
	private static final Color MUTED  = new Color(139, 147, 163).lock();
	private static final Color ACCENT = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CustomNodesUI.TEXT);
		// #region signal
		final BooleanSignal favorite = new BooleanSignal(false);
		final IntegerSignal changes = new IntegerSignal(0);
		// #endregion

		// #region dots
		ContainerNode
		.create(100, 130, 400, 140)
		.body(container -> {
			DotNode.create(20, 20, 100).attach(container);
			DotNode.create(150, 20, 100).attach(container);
			DotNode.create(280, 20, 100).attach(container);
		})
		.attach(this);
		// #endregion

		// #region swatch
		ContainerNode
		.create(600, 130, 560, 140)
		.body(container -> {
			SwatchNode
			.create(20, 20, 100)
			.color(CustomNodesUI.ACCENT)
			.selected(favorite)
			.onSelect(event -> changes.set(changes.get() + 1))
			.attach(container);
			TextNode
			.create(160, 30)
			.text(Text.create(() -> favorite.get() ? "Favorite" : "Plain", info))
			.attach(container);
			TextNode.create(160, 70).text(Text.create(() -> "Changes: " + changes.get(), info)).attach(container);
		})
		.attach(this);
		// #endregion

		// #region split
		SplitNode
		.create(120, 400, 520, 120)
		.body(split -> {
			RectNode.create(0, 0, 0, 0).color(CustomNodesUI.MUTED).attach(split);
			RectNode.create(0, 20, 0, 80).color(CustomNodesUI.MUTED).attach(split);
			RectNode.create(0, 0, 0, 0).color(CustomNodesUI.MUTED).attach(split);
		})
		.attach(this);
		// #endregion
	}

}