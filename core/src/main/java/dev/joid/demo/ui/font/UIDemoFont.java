package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.font.effect.DemoHighlightTextEffect;
import dev.joid.demo.ui.font.effect.DemoRainbowTextEffect;
import dev.joid.demo.ui.font.effect.DemoScrambleTextEffect;
import dev.joid.demo.ui.font.effect.DemoUnderlineTextEffect;
import dev.joid.demo.ui.font.effect.DemoWaveTextEffect;
import dev.joid.demo.ui.font.markup.DemoTextMarkup;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoFont extends UIDemo {

	private static final TextInfo MARKUP     = TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE).markups(DemoTextMarkup.inst());
	private static final TextInfo EFFECT     = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE).markups(DemoTextMarkup.inst());
	private static final MsdfFont LIGHT_BOLD = MsdfFont.create(DemoFont.MONTSERRAT.getFace(FontWeight.LIGHT, false), DemoFont.MONTSERRAT.getFace(FontWeight.BOLD, false));

	private static final TextInfo[] FONTS = {
			TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE).lineHeight(1.33F),
			TextInfo.create(DemoFont.PACIFICO, 20, Color.WHITE).lineHeight(1.425F),
			TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 20, Color.WHITE).lineHeight(1.425F)
	};

	private static final String[] TEXTS = {
			"lorem impsum",
			"italic",
			"spacing",
			"n-spacing",
			"AVATAR Tower WAVE LT Ty",
			"abcdefghijklmnopqrstuvwxyz",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			"0123456789 !?.,;:'\"()[]{}+-*/=%&@#",
			"àâéèêëîïôùûüç ÀÉÈÊÎÔÙÇ ß œ æ ł ı",
			"splitted text splitted text splitted text splitted text splitted splitted text splitted text splitted text splitted text splitted text splitted text",
			"overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow",
			"shadow text",
			"colored shadow text"
	};

	@Override
	public void init() {
		for (int i = 0; i < UIDemoFont.FONTS.length; i++) {
			final TextInfo info = UIDemoFont.FONTS[i];
			final Align align = i == 0 ? Align.START : i == 1 ? Align.CENTER : Align.END;
			FlexNode.vertical(10D + i * 640D, 10D, 620D).margin(3D).body(flex -> {
				for (final String text : UIDemoFont.TEXTS) {
					final boolean italic = "italic".equals(text);
					final boolean spacing = text.contains("spacing");
					final boolean negativeSpacing = text.contains("n-spacing");
					final boolean hasShadow = text.contains("shadow");
					final boolean hasColoredShadow = text.contains("colored");
					final boolean split = text.contains("splitted");
					final boolean overflow = text.contains("overflow");
					TextNode.create(0, 0).text(Text.create(text, info.copy().italic(italic).letterSpacing(negativeSpacing ? -0.16F : spacing ? 0.4F : 0F).shadow(hasShadow ? hasColoredShadow ? Color.RAINBOW() : Color.BLACK : null)).overflow(overflow ? TextOverflow.ELLIPSIS : TextOverflow.NONE).horizontalAlign(align)).mode(split ? TextMode.SPLIT : overflow ? TextMode.OVERFLOW : TextMode.NORMAL).width(flex.getWidth()).attach(flex);
				}
			}).attach(this);
		}

		FlexNode.horizontal(10D, 460D, 30D).body(flex -> {
			for (final FontWeight weight : FontWeight.values()) {
				TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, weight, 20, Color.WHITE))).width(211D).attach(flex);
			}
		}).attach(this);
		FlexNode.horizontal(10D, 494D, 30D).body(flex -> {
			for (final FontWeight weight : FontWeight.values()) {
				TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE).weight(weight).italic(true))).width(211D).attach(flex);
			}
		}).attach(this);
		FlexNode.horizontal(10D, 528D, 30D).body(flex -> {
			for (int value = 100; value <= 900; value += 100) {
				TextNode.create(0, 0).text(Text.create(value + " " + FontWeight.of(value), TextInfo.create(UIDemoFont.LIGHT_BOLD, FontWeight.of(value), 20, Color.WHITE))).width(211D).attach(flex);
			}
		}).attach(this);
		FlexNode.horizontal(10D, 562D, 30D).body(flex -> {
			for (int value = 100; value <= 900; value += 100) {
				TextNode.create(0, 0).text(Text.create(value + " " + FontWeight.of(value), TextInfo.create(UIDemoFont.LIGHT_BOLD, FontWeight.of(value), 20, Color.WHITE).italic(true).shadow(Color.BLACK))).width(211D).attach(flex);
			}
		}).attach(this);

		FlexNode.vertical(10D, 615D, 940D).margin(10D).body(flex -> {
			TextNode.create(0, 0).text(Text.create("<b>bold</b> regular <w=100>thin</w> <w=300>light</w> <w=600>semi bold</w> <w=900>black</w> <w=800>extra <b>bold</b> back</w>", UIDemoFont.MARKUP)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<i>italic</i> upright <b><i>bold italic</i></b> <w=100><i>thin italic</i></w>", UIDemoFont.MARKUP)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<c=ff5555>red</c> <c=55ff55>green</c> <c=5555ff>blue</c> <c=ffaa00>orange <b>bold</b> still orange</c> white", UIDemoFont.MARKUP)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<u>underline</u> <h>highlight</h> <u><h>both</h></u> <c=ff55ff><u>colored underline</u></c> <b><u>bold underline</u></b>", UIDemoFont.MARKUP)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<c=55ffff>the shadow <b>follows</b> the <u>markup</u></c> <h>and keeps the highlight behind</h>", UIDemoFont.MARKUP.copy().shadow(Color.BLACK))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<c=ff5555>colors</c> <c=55ff55>are</c> <b>ignored</b> when the text is not colored", UIDemoFont.MARKUP.copy().colored(false))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>raw</b> <c=ff5555>markup</c> <u>when</u> markups are disabled", UIDemoFont.MARKUP.copy().markups())).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>spaced</b> <c=ffff55>markup</c> <u>keeps</u> its <h>spacing</h>", UIDemoFont.MARKUP.copy().letterSpacing(0.3F))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>measured</b> <w=900>with</w> <c=55ff55>markup</c> <u>ends here</u>", UIDemoFont.MARKUP, Align.END)).width(flex.getWidth()).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>centered</b> <w=100>with</w> <i>markup</i>", UIDemoFont.MARKUP, Align.CENTER)).width(flex.getWidth()).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>bold</b> <i>italic</i> <c=ff5555>red</c> <u>underline</u> <h>highlight</h>", UIDemoFont.MARKUP.copy().font(DemoFont.PACIFICO))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>bold</b> <i>italic</i> <c=ff5555>red</c> <u>underline</u> <h>highlight</h>", UIDemoFont.MARKUP.copy().font(DemoFont.PLAYFAIR_DISPLAY))).attach(flex);
		}).attach(this);
		FlexNode.vertical(970D, 615D, 940D).margin(14D).body(flex -> {
			TextNode.create(0, 0).text(Text.create("a wave moves every glyph without touching the layout", UIDemoFont.EFFECT.copy().effects(DemoWaveTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("a rainbow colors the text glyph by glyph", UIDemoFont.EFFECT.copy().effects(DemoRainbowTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("scrambled characters keep their width", UIDemoFont.EFFECT.copy().effects(DemoScrambleTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("an underline and a highlight on the whole text", UIDemoFont.EFFECT.copy().effects(DemoUnderlineTextEffect.inst(), DemoHighlightTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("the shadow waves with the text", UIDemoFont.EFFECT.copy().effects(DemoWaveTextEffect.inst()).shadow(Color.BLACK))).attach(flex);
			TextNode.create(0, 0).text(Text.create("the shadow scrambles <u>like</u> the text", UIDemoFont.EFFECT.copy().effects(DemoScrambleTextEffect.inst()).shadow(Color.BLACK))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>markup</b> <u>adds</u> <h>effects</h> under the rainbow", UIDemoFont.EFFECT.copy().effects(DemoRainbowTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("spaced <u>wave</u> and <h>highlight</h>", UIDemoFont.EFFECT.copy().letterSpacing(0.36F).effects(DemoWaveTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<i>italic</i> <b>rainbow</b> <u>scramble</u>", UIDemoFont.EFFECT.copy().font(DemoFont.PACIFICO).effects(DemoRainbowTextEffect.inst(), DemoScrambleTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<w=300>light</w> <b>wave</b> with a <h>highlight</h>", UIDemoFont.EFFECT.copy().font(DemoFont.PLAYFAIR_DISPLAY).effects(DemoWaveTextEffect.inst()).shadow(Color.BLACK))).attach(flex);
		}).attach(this);
	}

}