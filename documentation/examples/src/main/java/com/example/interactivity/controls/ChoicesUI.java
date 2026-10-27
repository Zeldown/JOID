package com.example.interactivity.controls;

import com.example.ExampleFont;
import com.example.interactivity.kit.PlainSelectorNode;
import com.example.interactivity.kit.PlainStringSliderNode;
import com.example.interactivity.kit.PlainSwitchNode;
import com.example.interactivity.kit.PlainToggleNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class ChoicesUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final Signal<String> quality = new Signal<>("HIGH");
	private final BooleanSignal night = new BooleanSignal(false);
	private final Signal<String> language = new Signal<>("English");
	private final Signal<String> difficulty = new Signal<>("Normal");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(ChoicesUI.INK);

		RectNode
		.create(100, 140, 820, 380)
		.color(ChoicesUI.PLACEHOLDER)
		.body(card -> {
			// #region toggle
			PlainToggleNode
			.<String, String>create(60, 60, 160, 60)
			.state("Night", "Day")
			.toggle(this.night)
			.attach(card);
			// #endregion
			TextNode.create(60, 160).text(Text.create("Night: " + this.night.get(), info)).attach(card);
		})
		.attach(this);

		RectNode
		.create(1000, 140, 820, 380)
		.color(ChoicesUI.PLACEHOLDER)
		.body(card -> {
			// #region switch
			PlainSwitchNode
			.<String>create(60, 60, 600, 60)
			.values("Story", "Normal", "Hard")
			.value(this.difficulty)
			.attach(card);
			// #endregion
			TextNode.create(60, 160).text(Text.create("Difficulty: " + this.difficulty.get(), info)).attach(card);
		})
		.attach(this);

		RectNode
		.create(100, 560, 820, 380)
		.color(ChoicesUI.PLACEHOLDER)
		.body(card -> {
			TextNode.create(400, 60).text(Text.create("Language: " + this.language.get(), info)).attach(card);
			// #region selector
			PlainSelectorNode
			.<String>create(60, 40, 280, 60)
			.values("English", "French", "German", "Japanese")
			.value(this.language)
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1000, 560, 820, 380)
		.color(ChoicesUI.PLACEHOLDER)
		.body(card -> {
			// #region slider
			PlainStringSliderNode
			.create(60, 60, 600, 40)
			.values(Quality.values())
			.value(this.quality)
			.attach(card);
			// #endregion
			TextNode.create(60, 160).text(Text.create("Quality: " + this.quality.get(), info)).attach(card);
		})
		.attach(this);
	}

	// #region quality
	public enum Quality {

		LOW,
		MEDIUM,
		HIGH,
		ULTRA;

	}
	// #endregion

}