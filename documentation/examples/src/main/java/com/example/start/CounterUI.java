package com.example.start;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class CounterUI extends UI {

	private static final MsdfFont FONT = MsdfFontLoader.load(
		CounterUI.class.getResourceAsStream("/fonts/Montserrat/Montserrat-Regular.ttf")
	).join();

	private static final Color INK  = new Color(153, 153, 153).lock();
	private static final Color DARK = new Color(85, 85, 85).lock();

	private final IntegerSignal clicks = new IntegerSignal(0);

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(CounterUI.FONT, 40F).color(Color.WHITE);

		RectNode
		.create(760, 480, 400, 120)
		.color(CounterUI.INK)
		.hoveredColor(CounterUI.DARK)
		.onClick(event -> this.clicks.increment())
		.label(Text.create("Clicks: " + this.clicks.get(), label))
		.attach(this);
	}

}