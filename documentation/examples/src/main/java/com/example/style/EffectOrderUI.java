package com.example.style;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class EffectOrderUI extends UI {

	private static final Color LINE      = new Color(74, 82, 98).lock();
	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(EffectOrderUI.TEXT);
		final TextInfo light = TextInfo.create(ExampleFont.REGULAR, 24F).color(EffectOrderUI.ON_ACCENT);

		RectNode
		.create(410, 220, 1100, 640)
		.color(EffectOrderUI.SURFACE)
		.borderColor(EffectOrderUI.LINE)
		.borderStroke(2D)
		.effect(RoundedNodeEffect.create(12F))
		.body(outer -> {
			TextNode.create(30, 24).text(Text.create("1. The wrapping effects start, lowest priority first:", info)).attach(outer);
			TextNode.create(30, 60).text(Text.create("transform (-100), shadow (-50), mask (0), yours (0)", info)).attach(outer);
			RectNode
			.create(30, 110, 1040, 440)
			.color(EffectOrderUI.ELEMENT)
			.borderColor(EffectOrderUI.LINE)
			.borderStroke(2D)
			.effect(RoundedNodeEffect.create(12F))
			.body(inner -> {
				RectNode
				.create(30, 30, 480, 290)
				.color(EffectOrderUI.ACCENT)
				.effect(RoundedNodeEffect.create(12F))
				.body(node -> {
					TextNode.create(24, 24).text(Text.create("2. The node draws itself", light)).attach(node);
					TextNode.create(24, 90).text(Text.create("3. Its SELF passes, by priority:", light)).attach(node);
					TextNode.create(24, 130).text(Text.create("rounded or circle (100)", light)).attach(node);
					TextNode.create(24, 170).text(Text.create("blur (150)", light)).attach(node);
					TextNode.create(24, 210).text(Text.create("border (200)", light)).attach(node);
				})
				.attach(inner);
				RectNode
				.create(530, 30, 480, 290)
				.color(EffectOrderUI.ELEMENT)
				.borderColor(EffectOrderUI.LINE)
				.borderStroke(2D)
				.effect(RoundedNodeEffect.create(12F))
				.body(children -> {
					TextNode.create(24, 24).text(Text.create("4. The children draw on top", info)).attach(children);
				})
				.attach(inner);
				TextNode.create(30, 360).text(Text.create("5. The CHILDREN passes run over the node and its children", info)).attach(inner);
			})
			.attach(outer);
			TextNode.create(30, 580).text(Text.create("6. The wrapping effects end, in reverse order", info)).attach(outer);
		})
		.attach(this);
	}

}