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
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;

public class UIDemoTextEffect extends UIDemo {

	private static final TextInfo INFO = TextInfo.create(DemoFont.MONTSERRAT, 36, Color.WHITE).markups(DemoTextMarkup.inst());

	@Override
	public void init() {
		FlexNode.vertical(10D, 20D, 1900D).margin(22D).body(flex -> {
			TextNode.create(0, 0).text(Text.create("a wave moves every glyph without touching the layout", UIDemoTextEffect.INFO.copy().effects(DemoWaveTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("a rainbow colors the text glyph by glyph", UIDemoTextEffect.INFO.copy().effects(DemoRainbowTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("scrambled characters keep their width", UIDemoTextEffect.INFO.copy().effects(DemoScrambleTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("an underline and a highlight on the whole text", UIDemoTextEffect.INFO.copy().effects(DemoUnderlineTextEffect.inst(), DemoHighlightTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("the shadow waves with the text", UIDemoTextEffect.INFO.copy().effects(DemoWaveTextEffect.inst()).shadow(Color.BLACK))).attach(flex);
			TextNode.create(0, 0).text(Text.create("the shadow scrambles <u>like</u> the text", UIDemoTextEffect.INFO.copy().effects(DemoScrambleTextEffect.inst()).shadow(Color.BLACK))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>markup</b> <u>adds</u> <h>effects</h> under the rainbow", UIDemoTextEffect.INFO.copy().effects(DemoRainbowTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("spaced <u>wave</u> and <h>highlight</h>", UIDemoTextEffect.INFO.copy().letterSpacing(10F).effects(DemoWaveTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<i>italic</i> <b>rainbow</b> <u>scramble</u>", UIDemoTextEffect.INFO.copy().font(DemoFont.BATUPHAT).effects(DemoRainbowTextEffect.inst(), DemoScrambleTextEffect.inst()))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<w=300>light</w> <b>wave</b> with a <h>highlight</h>", UIDemoTextEffect.INFO.copy().font(DemoFont.SPACE_GROTESK).effects(DemoWaveTextEffect.inst()).shadow(Color.BLACK))).attach(flex);
		}).attach(this);
	}

}