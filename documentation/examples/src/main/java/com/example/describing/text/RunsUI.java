package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextElement;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class RunsUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private int clicks;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(RunsUI.TEXT);
		final TextInfo bold = info.withWeight(FontWeight.BOLD).withColor(RunsUI.ACCENT);

		RectNode
		.create(610, 300, 700, 270)
		.color(RunsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(RunsUI.ELEMENT, 1.5F))
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
			.color(RunsUI.ELEMENT)
			.onClick(event -> this.clicks++)
			.body(button -> {
				TextNode
				.create(0, 0, 400, 80)
				.text(Text.create(() -> "Clicks: " + this.clicks, info).align(Align.CENTER))
				.attach(button);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(610, 600, 700, 170)
		.color(RunsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(RunsUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region measure
			final String label = "Settings";
			RectNode
			.create(50, 50, info.aw(label, 48D), info.ah(24D))
			.color(RunsUI.ELEMENT)
			.body(box -> TextNode.create(24, 12).text(Text.create(label, info)).attach(box))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}