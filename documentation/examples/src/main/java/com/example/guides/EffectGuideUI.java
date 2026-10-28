package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EffectGuideUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(EffectGuideUI.TEXT);
		// #region count
		final IntegerSignal unread = new IntegerSignal(3);
		// #endregion

		// #region use
		RectNode
		.create(810, 500, 300, 80)
		.color(EffectGuideUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(EffectGuideUI.ELEMENT, 1.5F))
		.label(Text.create("Inbox", info))
		.effect(RoundedNodeEffect.create(16F))
		.effect(BadgeNodeEffect.create(EffectGuideUI.ACCENT).shown(unread.get() > 0))
		.onClick(event -> unread.set(0))
		.attach(this);
		// #endregion
	}

}