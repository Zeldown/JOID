package com.example.catalog;

import com.example.ExampleFont;
import com.example.kit.KitCheckboxNode;
import com.example.kit.KitDoubleSliderNode;
import com.example.kit.KitIntegerSliderNode;
import com.example.kit.KitScrollbarNode;
import com.example.kit.KitSelectorNode;
import com.example.kit.KitStringSliderNode;
import com.example.kit.KitSwitchNode;
import com.example.kit.KitToggleNode;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.control.field.MultilineTextFieldNode;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class ControlsCatalogUI extends CatalogUI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(CatalogUI.TEXT);

		super.card(0).body(card -> TextFieldNode.create(30, 125, 280, 50).info(info).margin(12D).background(RectNode.create(0, 0, 0, 0).color(CatalogUI.ELEMENT)).text("Text field").attach(card)).attach(this);

		super.card(1).body(card -> {
			MultilineTextFieldNode
			.create(30, 50, 280, 200)
			.info(info)
			.margin(12D)
			.background(RectNode.create(0, 0, 0, 0).color(CatalogUI.ELEMENT))
			.text("A text field that wraps its text on several lines")
			.attach(card);
		})
		.attach(this);

		super.card(2).body(card -> IntegerFieldNode.create(70, 125, 200, 50).value(42).info(info).margin(12D).background(RectNode.create(0, 0, 0, 0).color(CatalogUI.ELEMENT)).attach(card)).attach(this);

		super.card(3).body(card -> {
			KitCheckboxNode.create(80, 120, 60).attach(card);
			KitCheckboxNode.create(200, 120, 60).checked(true).attach(card);
		})
		.attach(this);

		super.card(4).body(card -> {
			KitToggleNode.<Boolean, Boolean>create(70, 90, 200, 50).attach(card);
			KitToggleNode.<Boolean, Boolean>create(70, 160, 200, 50).toggle(true).attach(card);
		})
		.attach(this);

		super.card(5).body(card -> KitSwitchNode.<String>create(30, 125, 280, 50).values("One", "Two", "Three").valueIndex(1).attach(card)).attach(this);

		super.card(6).body(card -> KitSelectorNode.<String>create(40, 125, 260, 50).values("First", "Second", "Third").attach(card)).attach(this);

		super.card(7).body(card -> KitIntegerSliderNode.create(40, 130, 260, 40).range(0, 10).value(4).attach(card)).attach(this);

		super.card(8).body(card -> KitDoubleSliderNode.create(40, 130, 260, 40).range(0D, 1D, 0.25D).value(0.75D).attach(card)).attach(this);

		super.card(9).body(card -> {
			KitStringSliderNode.create(40, 110, 260, 40).values(Level.values()).value(Level.MEDIUM).attach(card);
			TextNode.create(20, 165, 80, 30).text(Text.create("Low", info).alignX(Align.CENTER)).attach(card);
			TextNode.create(130, 165, 80, 30).text(Text.create("Medium", info).alignX(Align.CENTER)).attach(card);
			TextNode.create(240, 165, 80, 30).text(Text.create("High", info).alignX(Align.CENTER)).attach(card);
		})
		.attach(this);

		super.card(10).body(card -> {
			ContainerNode
			.create(40, 40, 260, 220)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(KitScrollbarNode.create(248, 0, 12, 220))
			.body(area -> {
				FlexNode
				.vertical(0, 0, 236)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 6; i++) {
						RectNode.create(0, 0, 236, 60).color(CatalogUI.ELEMENT).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(card);
		})
		.attach(this);
	}

	public enum Level {

		LOW,
		MEDIUM,
		HIGH;

	}

}