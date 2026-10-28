package com.example.interactivity.controls;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNode;
import dev.joid.lib.ui.node.impl.control.selector.SelectorNode;
import dev.joid.lib.ui.node.impl.control.slider.StringSliderNode;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class ChoicesUI extends UI {

	private static final Color HOVER     = new Color(54, 61, 77).lock();
	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	private final Signal<String> quality    = new Signal<>("HIGH");
	private final BooleanSignal  night      = new BooleanSignal(false);
	private final Signal<String> language   = new Signal<>("English");
	private final Signal<String> difficulty = new Signal<>("Normal");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ChoicesUI.TEXT);
		final TextInfo selected = TextInfo.create(ExampleFont.REGULAR, 24F).color(ChoicesUI.ON_ACCENT);

		RectNode
		.create(100, 140, 820, 250)
		.color(ChoicesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ChoicesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region toggle
			new ToggleNode<String, String>(60, 60, 160, 60) {}
			.track(RectNode.create(0, 0, 0, 0).color(ChoicesUI.ELEMENT))
			.knob(RectNode.create(0, 0, 0, 0).color(ChoicesUI.ACCENT))
			.state("Night", "Day")
			.toggle(this.night)
			.attach(card);
			// #endregion
			TextNode.create(60, 160).text(Text.create("Night: " + this.night.get(), info)).attach(card);
		})
		.attach(this);

		RectNode
		.create(1000, 140, 820, 250)
		.color(ChoicesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ChoicesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region switch
			new SwitchNode<String>(60, 60, 600, 60) {}
			.option((value, index) -> RectNode
			.create(0, 0, 0, 0)
			.color(() -> value.equals(this.difficulty.get()) ? ChoicesUI.ACCENT : ChoicesUI.ELEMENT)
			.label(() -> Text.create(value, value.equals(this.difficulty.get()) ? selected : info)))
			.values("Story", "Normal", "Hard")
			.value(this.difficulty)
			.attach(card);
			// #endregion
			TextNode.create(60, 160).text(Text.create("Difficulty: " + this.difficulty.get(), info)).attach(card);
		})
		.attach(this);

		RectNode
		.create(100, 560, 820, 380)
		.color(ChoicesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ChoicesUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(400, 60).text(Text.create("Language: " + this.language.get(), info)).attach(card);
			// #region selector
			new SelectorNode<String>(60, 40, 280, 60) {}
			.field(RectNode.create(0, 0, 0, 0).color(ChoicesUI.ACCENT))
			.option(value -> RectNode
			.create(0, 0, 0, 0)
			.color(ChoicesUI.ELEMENT)
			.hoveredColor(ChoicesUI.HOVER)
			.label(Text.create(value, info)))
			.values("English", "French", "German", "Japanese")
			.value(this.language)
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1000, 560, 820, 250)
		.color(ChoicesUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ChoicesUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region slider
			new StringSliderNode(60, 60, 600, 40) {}
			.values(Quality.values())
			.track(RectNode.create(0, 0, 0, 0).color(ChoicesUI.ELEMENT))
			.thumb(RectNode.create(0, 0, 0, 0).color(ChoicesUI.ACCENT))
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