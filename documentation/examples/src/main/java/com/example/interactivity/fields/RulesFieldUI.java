package com.example.interactivity.fields;

import com.example.ExampleFont;
import com.example.kit.KitTextFieldNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class RulesFieldUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final StringSignal code = new StringSignal("");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(RulesFieldUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(RulesFieldUI.PLACEHOLDER)
		.body(card -> {
			// #region rules
			KitTextFieldNode
			.create(60, 60, 480, 60)
			.info(info)
			.placeholder("Code")
			.accept(text -> text.matches("[A-Za-z ]*"))
			.maxTextLength(6)
			.normalize(text -> text.trim().toUpperCase())
			.text(this.code)
			.attach(card);
			// #endregion
			TextNode.create(60, 170).text(Text.create("Signal: " + this.code.get(), info)).attach(card);
		})
		.attach(this);
	}

}