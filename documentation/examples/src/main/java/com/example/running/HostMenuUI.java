package com.example.running;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class HostMenuUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 32F).color(HostMenuUI.TEXT);
		final TextInfo hint = TextInfo.create(ExampleFont.REGULAR, 22F).color(HostMenuUI.MUTED);

		RectNode
		.create(300, 140, 480, 200)
		.color(HostMenuUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(HostMenuUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(0, 40, 480, 50).text(Text.create("Menu", title).align(Align.CENTER)).attach(card);
			TextNode
			.create(0, 110, 480, 40)
			.text(Text.create("Escape: back to the game", hint).align(Align.CENTER))
			.attach(card);
		})
		.attach(this);
	}

}