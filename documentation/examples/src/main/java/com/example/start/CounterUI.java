package com.example.start;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CounterUI extends UI {

	private static final MsdfFont FONT = MsdfFontLoader.load(
		"fonts/Montserrat/Montserrat-Regular.ttf"
	).join();

	private static final Color ACTIVE    = new Color(125, 164, 255).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	private final IntegerSignal clicks = new IntegerSignal(0);

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(CounterUI.FONT, 40F).color(CounterUI.ON_ACCENT);

		RectNode
		.create(760, 480, 400, 120)
		.color(CounterUI.ACCENT)
		.hoveredColor(CounterUI.ACTIVE)
		.effect(RoundedNodeEffect.create(12F))
		.onClick(event -> this.clicks.increment())
		.label(Text.create("Clicks: " + this.clicks.get(), label))
		.attach(this);
	}

}