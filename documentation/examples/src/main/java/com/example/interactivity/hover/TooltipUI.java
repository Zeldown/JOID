package com.example.interactivity.hover;

import java.util.Arrays;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.hover.impl.CustomHoverElement;
import dev.joid.lib.ui.node.hover.impl.NodeHoverElement;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TooltipUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(TooltipUI.INK);
		final TextInfo white = TextInfo.create(ExampleFont.REGULAR, 22F).color(Color.WHITE);
		final IntegerSignal coins = new IntegerSignal(120);

		RectNode
		.create(60, 340, 560, 400)
		.color(TooltipUI.PLACEHOLDER)
		.body(card -> {
			// #region text
			RectNode
			.create(60, 60, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> coins.add(10))
			.hover(() -> "Coins: " + coins.peek())
			.label(Text.create("Wallet", info))
			.attach(card);
			RectNode
			.create(300, 60, 200, 60)
			.color(Color.WHITE)
			.hover(() -> Arrays.asList("Shop", "Opens at noon"))
			.label(Text.create("Shop", info))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(680, 340, 560, 400)
		.color(TooltipUI.PLACEHOLDER)
		.body(card -> {
			// #region node
			RectNode
			.create(180, 140, 200, 60)
			.color(Color.WHITE)
			.hover(NodeHoverElement.relative(RectNode.create(0, -60, 200, 50).color(TooltipUI.INK).label(Text.create("Up", white))))
			.label(Text.create("Profile", info))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1300, 340, 560, 400)
		.color(TooltipUI.PLACEHOLDER)
		.body(card -> {
			// #region custom
			RectNode
			.create(180, 140, 200, 60)
			.color(Color.WHITE)
			.hover(CustomHoverElement.follow((node, mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawRoundedBorder(-24D, -24D, 48D, 48D, TooltipUI.INK, 24F, 3D);
			}))
			.label(Text.create("Target", info))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}