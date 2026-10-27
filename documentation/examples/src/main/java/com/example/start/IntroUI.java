package com.example.start;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class IntroUI extends UI {

	private static final Color INK  = new Color(153, 153, 153).lock();
	private static final Color DARK = new Color(85, 85, 85).lock();

	private final BooleanSignal notifications = new BooleanSignal(false);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(ExampleFont.REGULAR, 40F).color(IntroUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 36F).color(Color.WHITE);

		RectNode
		.create(660, 440, 600, 200)
		.color(Color.WHITE)
		.effect(RoundedNodeEffect.create(24F))
		.effect(ShadowNodeEffect.create(IntroUI.INK.withAlpha(0.4F), 40F).offsetY(12D))
		.body(card -> {
			TextNode
			.create(48, card.dh(2))
			.text(Text.create("Notifications", caption))
			.anchorY(Align.CENTER)
			.attach(card);
			RectNode
			.create(392, 60, 160, 80)
			.color(this.notifications.get() ? IntroUI.DARK : IntroUI.INK)
			.effect(RoundedNodeEffect.create(16F))
			.onClick(event -> this.notifications.toggle())
			.label(Text.create(this.notifications.get() ? "On" : "Off", label))
			.attach(card);
		})
		.attach(this);
	}

}