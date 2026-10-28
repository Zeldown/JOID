package com.example.start;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class IntroUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color SHADOW    = new Color(0, 0, 0, 153).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	private final BooleanSignal notifications = new BooleanSignal(false);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(ExampleFont.REGULAR, 40F).color(IntroUI.TEXT);
		final TextInfo on = TextInfo.create(ExampleFont.REGULAR, 36F).color(IntroUI.ON_ACCENT);
		final TextInfo off = TextInfo.create(ExampleFont.REGULAR, 36F).color(IntroUI.TEXT);

		RectNode
		.create(660, 440, 600, 200)
		.color(IntroUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(IntroUI.ELEMENT, 1.5F))
		.effect(ShadowNodeEffect.create(IntroUI.SHADOW, 36F).offsetY(12D))
		.body(card -> {
			TextNode
			.create(48, card.dh(2))
			.text(Text.create("Notifications", caption))
			.anchorY(Align.CENTER)
			.attach(card);
			RectNode
			.create(392, 60, 160, 80)
			.color(this.notifications.get() ? IntroUI.ACCENT : IntroUI.ELEMENT)
			.effect(RoundedNodeEffect.create(8F))
			.onClick(event -> this.notifications.toggle())
			.label(() -> Text.create(this.notifications.get() ? "On" : "Off", this.notifications.get() ? on : off))
			.attach(card);
		})
		.attach(this);
	}

}