package com.example.interactivity.controls;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class SettingsUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	// #region signals
	private final IntegerSignal volume = new IntegerSignal(7);
	private final BooleanSignal subtitles = new BooleanSignal(true);
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(SettingsUI.TEXT);

		RectNode
		.create(560, 340, 800, 400)
		.color(SettingsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(SettingsUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(60, 55).text(Text.create("Subtitles", info)).attach(card);
			TextNode.create(60, 145).text(Text.create("Volume", info)).attach(card);
			// #region bind
			// #region checkbox
			new CheckboxNode(240, 40, 50, 50) {}
			.box(RectNode.create(0, 0, 0, 0).color(SettingsUI.ELEMENT))
			.mark(RectNode.create(0, 0, 25, 25).color(SettingsUI.ACCENT))
			.checked(this.subtitles)
			.attach(card);
			// #endregion
			new IntegerSliderNode(240, 140, 300, 40) {}
			.range(0, 10)
			.track(RectNode.create(0, 0, 0, 0).color(SettingsUI.ELEMENT))
			.thumb(RectNode.create(0, 0, 0, 0).color(SettingsUI.ACCENT))
			.value(this.volume)
			.attach(card);
			IntegerFieldNode
			.create(580, 130, 160, 60)
			.min(0)
			.max(10)
			.value(this.volume)
			.info(info)
			.background(RectNode.create(0, 0, 0, 0).color(SettingsUI.ELEMENT))
			.marginHorizontal(12D)
			.attach(card);
			// #endregion
			TextNode.create(60, 260).text(Text.create("Subtitles: " + this.subtitles.get(), info)).attach(card);
			TextNode.create(60, 310).text(Text.create("Volume: " + this.volume.get(), info)).attach(card);
		})
		.attach(this);
	}

}