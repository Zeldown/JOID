package com.example.interactivity.controls;

import com.example.ExampleFont;
import com.example.kit.KitCheckboxNode;
import com.example.kit.KitIntegerFieldNode;
import com.example.kit.KitIntegerSliderNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class SettingsUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	// #region signals
	private final IntegerSignal volume = new IntegerSignal(7);
	private final BooleanSignal subtitles = new BooleanSignal(true);
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(SettingsUI.INK);

		RectNode
		.create(560, 340, 800, 400)
		.color(SettingsUI.PLACEHOLDER)
		.body(card -> {
			TextNode.create(60, 55).text(Text.create("Subtitles", info)).attach(card);
			TextNode.create(60, 145).text(Text.create("Volume", info)).attach(card);
			// #region bind
			// #region checkbox
			KitCheckboxNode.create(240, 40, 50).checked(this.subtitles).attach(card);
			// #endregion
			KitIntegerSliderNode.create(240, 140, 300, 40).range(0, 10).value(this.volume).attach(card);
			KitIntegerFieldNode.create(580, 130, 160, 60).min(0).max(10).value(this.volume).info(info).attach(card);
			// #endregion
			TextNode.create(60, 260).text(Text.create("Subtitles: " + this.subtitles.get(), info)).attach(card);
			TextNode.create(60, 310).text(Text.create("Volume: " + this.volume.get(), info)).attach(card);
		})
		.attach(this);
	}

}