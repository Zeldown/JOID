package com.example.tutorial.polish;

import dev.joid.lib.JOID;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIDataPopup(active = true)
@UIData(background = true, backgroundColor = "#00000080")
public class SettingsUI extends UI {

	@Override
	public void init() {
		final TextInfo heading = TextInfo.create(Theme.FONT, 40F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final TextInfo caption = TextInfo.create(Theme.FONT, 30F).color(Theme.TEXT);
		final TextInfo label = TextInfo.create(Theme.FONT, 30F).color(Theme.TEXT);
		final SettingsStore settings = super.useStore(SettingsStore.class, SettingsStore::new);

		RectNode
		.create(660, 290, 600, 500)
		.color(Theme.SURFACE)
		.effect(RoundedNodeEffect.create(24F))
		.effect(ShadowNodeEffect.create(Theme.SHADOW, 40F).offsetY(12D))
		.body(card -> {
			TextNode.create(48, 40).text(Text.create("Settings", heading)).attach(card);
			TextNode.create(48, 165).text(Text.create("Music", caption)).anchorY(Align.CENTER).attach(card);
			RectNode
			.create(392, 130, 160, 70)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.cursor(Cursor.POINTER)
			.effect(RoundedNodeEffect.create(12F))
			.onClick(event -> settings.getMusic().toggle())
			.label(Text.create(settings.getMusic().get() ? "On" : "Off", label))
			.attach(card);
			TextNode.create(48, 275).text(Text.create("Volume", caption)).anchorY(Align.CENTER).attach(card);
			RectNode
			.create(312, 240, 70, 70)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.cursor(Cursor.POINTER)
			.effect(RoundedNodeEffect.create(12F))
			.onClick(event -> settings.getVolume().set(Math.max(0, settings.getVolume().peek() - 1)))
			.label(Text.create("-", label))
			.attach(card);
			TextNode
			.create(432, 275)
			.text(Text.create(settings.getVolume().get() * 10 + " %", caption))
			.anchor(Align.CENTER)
			.attach(card);
			RectNode
			.create(482, 240, 70, 70)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.cursor(Cursor.POINTER)
			.effect(RoundedNodeEffect.create(12F))
			.onClick(event -> settings.getVolume().set(Math.min(10, settings.getVolume().peek() + 1)))
			.label(Text.create("+", label))
			.attach(card);
			RectNode
			.create(48, 380, 504, 80)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.cursor(Cursor.POINTER)
			.effect(RoundedNodeEffect.create(12F))
			.onClick(event -> JOID.close(this))
			.label(Text.create("Close", label))
			.attach(card);
		})
		.attach(this);
	}

}