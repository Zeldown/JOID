package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class EffectGuideUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(EffectGuideUI.INK);
		// #region count
		final IntegerSignal unread = new IntegerSignal(3);
		// #endregion

		// #region use
		RectNode
		.create(810, 500, 300, 80)
		.color(Color.WHITE)
		.label(Text.create("Inbox", info))
		.effect(RoundedNodeEffect.create(16F))
		.effect(BadgeNodeEffect.create(EffectGuideUI.INK).shown(unread.get() > 0))
		.onClick(event -> unread.set(0))
		.attach(this);
		// #endregion
	}

}