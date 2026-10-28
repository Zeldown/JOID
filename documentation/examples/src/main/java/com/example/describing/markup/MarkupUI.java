package com.example.describing.markup;

import java.time.Duration;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.converter.TextConverter;
import dev.joid.lib.font.modifier.TextModifier;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class MarkupUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	private static final TagTextMarkup         MARKUP   = new TagTextMarkup();
	private static final DurationTextConverter DURATION = new DurationTextConverter();

	@Override
	public void init() {
		// #region register
		TextConverter.register(MarkupUI.DURATION);
		// #endregion

		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(MarkupUI.TEXT);

		RectNode
		.create(560, 315, 800, 450)
		.color(MarkupUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(MarkupUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region markup
			final TextInfo tagged = info.withMarkups(MarkupUI.MARKUP);
			TextNode.create(50, 50).text(Text.create("Read the <b>terms</b>, then <em>accept</em> them", tagged)).attach(card);
			// #endregion
			// #region effects
			TextNode
			.create(50, 150)
			.text(Text.create("A wave moves every glyph", info.withEffects(WaveTextEffect.inst())))
			.attach(card);
			// #endregion
			// #region modifier
			TextNode
			.create(50, 250)
			.text(Text.create("<b>loud</b> and clear", tagged).modifier(TextModifier.UPPER_CASE))
			.attach(card);
			// #endregion
			// #region converter
			TextNode.create(50, 350).text(Text.create(Duration.ofSeconds(95L), info)).attach(card);
			// #endregion
		})
		.attach(this);
	}

}