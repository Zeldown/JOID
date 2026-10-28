package com.example.catalog;

import com.example.ExampleFont;
import com.example.describing.markup.TagTextMarkup;
import com.example.describing.markup.UnderlineTextEffect;
import com.example.describing.markup.WaveTextEffect;
import com.example.text.HighlightTextEffect;
import com.example.text.RainbowTextEffect;
import com.example.text.ScrambleTextEffect;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.modifier.TextModifier;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class TextCatalogUI extends CatalogUI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 30F).color(CatalogUI.TEXT);
		final ITextEffect[] effects = {new WaveTextEffect(), new RainbowTextEffect(), UnderlineTextEffect.INSTANCE, new HighlightTextEffect(), new ScrambleTextEffect()};
		final TextModifier[] modifiers = {TextModifier.UPPER_CASE, TextModifier.LOWER_CASE, TextModifier.CAPITALIZE, TextModifier.WORD_CAPITALIZE, TextModifier.CAMEL_CASE, TextModifier.UPPER_CAMEL_CASE, TextModifier.SNAKE_CASE};

		for (int i = 0; i < effects.length; i++) {
			final TextInfo style = info.withEffects(effects[i]);
			final String sentence = effects[i] instanceof RainbowTextEffect ? "Every glyph" : "Text effects move every glyph";
			super.card(i).body(card -> TextNode.create(30, 115, 280, 0).text(Text.create(sentence, style).alignX(Align.CENTER)).mode(TextMode.SPLIT).attach(card)).attach(this);
		}

		super.card(5).body(card -> TextNode.create(30, 115, 280, 0).text(Text.create("A <b>bold</b> word and an <em>underlined</em> one", info.withMarkups(new TagTextMarkup())).alignX(Align.CENTER)).mode(TextMode.SPLIT).attach(card)).attach(this);

		for (int i = 0; i < modifiers.length; i++) {
			final TextModifier modifier = modifiers[i];
			super.card(6 + i).body(card -> TextNode.create(0, 0, 340, 300).text(Text.create("hello joid world", info).align(Align.CENTER).modifier(modifier)).attach(card)).attach(this);
		}
	}

}