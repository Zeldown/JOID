package com.example.interactivity.hover;

import java.util.Arrays;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.hover.impl.CustomHoverElement;
import dev.joid.lib.ui.node.hover.impl.NodeHoverElement;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TooltipUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(TooltipUI.TEXT);
		final TextInfo up = TextInfo.create(ExampleFont.REGULAR, 22F).color(TooltipUI.ON_ACCENT);
		final IntegerSignal coins = new IntegerSignal(120);

		RectNode
		.create(60, 340, 560, 400)
		.color(TooltipUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TooltipUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region text
			RectNode
			.create(60, 60, 200, 60)
			.color(TooltipUI.ELEMENT)
			.onClick(event -> coins.add(10))
			.hover(() -> "Coins: " + coins.peek())
			.label(Text.create("Wallet", info))
			.attach(card);
			RectNode
			.create(300, 60, 200, 60)
			.color(TooltipUI.ELEMENT)
			.hover(() -> Arrays.asList("Shop", "Opens at noon"))
			.label(Text.create("Shop", info))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(680, 340, 560, 400)
		.color(TooltipUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TooltipUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region node
			RectNode
			.create(180, 140, 200, 60)
			.color(TooltipUI.ELEMENT)
			.hover(NodeHoverElement.relative(RectNode.create(0, -60, 200, 50).color(TooltipUI.ACCENT).label(Text.create("Up", up))))
			.label(Text.create("Profile", info))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1300, 340, 560, 400)
		.color(TooltipUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TooltipUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region custom
			RectNode
			.create(180, 140, 200, 60)
			.color(TooltipUI.ELEMENT)
			.hover(CustomHoverElement.follow((node, mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawRect(-16D, -1.5D, 32D, 3D, TooltipUI.ACCENT);
				DrawUtils.SHAPE.drawRect(-1.5D, -16D, 3D, 32D, TooltipUI.ACCENT);
			}))
			.label(Text.create("Target", info))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}