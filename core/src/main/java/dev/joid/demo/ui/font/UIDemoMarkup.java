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
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.TextOverflow;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoMarkup extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoMarkup.INK);
		final TextInfo markup = TextInfo.create(DemoFont.MONTSERRAT, 20, UIDemoMarkup.INK).markups(DemoTextMarkup.inst());

		RectNode
		.create(80, 30, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>bold</b> regular <w=100>thin</w> <w=300>light</w> <w=600>semi bold</w> <w=900>black</w> <w=800>extra <b>bold</b> back</w>", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Weights", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 30, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<i>italic</i> upright <b><i>bold italic</i></b> <w=100><i>thin italic</i></w>", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Italic", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 30, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<c=cc3333>red</c> <c=2e8b57>green</c> <c=3355cc>blue</c> <c=e67e00>orange <b>bold</b> still orange</c> ink", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Colors", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 30, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<u>underline</u> <h>highlight</h> <u><h>both</h></u> <c=cc3333><u>colored underline</u></c> <b><u>bold underline</u></b>", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Decorations", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 30, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>bold <i>bold italic <c=3355cc>blue <u>underlined</u></c></i></b> back to ink", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(20, 110, 280, 0).text(Text.create("<f=pacifico>Pacifico <c=3355cc>blue</c></f> <f=playfair><i>Playfair</i></f> ink", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Tags and fonts", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 280, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<c=3355cc>the shadow <b>follows</b> the <u>markup</u></c> <h>behind the highlight</h>", markup.copy().shadow(Color.BLACK))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Shadow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 280, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<c=cc3333>colors</c> <c=2e8b57>are</c> <b>ignored</b> when the text is not colored", markup.copy().colored(false))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Uncolored", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 280, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>raw</b> <c=cc3333>tags</c> <u>stay</u> text", markup.copy().markups())).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Markup off", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 280, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>spaced</b> <c=3355cc>markup</c> <u>keeps</u> its <h>spacing</h>", markup.copy().letterSpacing(0.3F))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Spaced", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 280, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>ends</b> <c=3355cc>here</c>", markup, Align.END)).attach(rect);
			TextNode.create(20, 80, 280, 0).text(Text.create("<b>centered</b> <i>markup</i>", markup, Align.CENTER)).attach(rect);
			TextNode.create(160, 205).text(Text.create("Aligned", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 530, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>Bold words</b> and <c=cc3333>a colored phrase that wraps over several lines</c> keep their style", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Wrapped", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 530, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>bold</b> and <c=cc3333>red text cut by the width</c>", markup, TextOverflow.ELLIPSIS)).mode(TextMode.OVERFLOW).attach(rect);
			TextNode.create(160, 205).text(Text.create("Cut", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 530, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<i>italic</i> <c=cc3333>red</c> <u>underline</u> <h>highlight</h>", markup.copy().font(DemoFont.PACIFICO))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Pacifico", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 530, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<i>italic</i> <c=cc3333>red</c> <u>underline</u> <h>highlight</h>", markup.copy().font(DemoFont.PLAYFAIR_DISPLAY))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Playfair Display", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 530, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("an underline and a highlight on the whole text", markup.copy().effects(DemoUnderlineTextEffect.inst(), DemoHighlightTextEffect.inst()))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Whole text", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 780, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 30, 280, 0).text(Text.create("a wave moves every glyph without touching the layout", markup.copy().effects(DemoWaveTextEffect.inst()))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Wave", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 780, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("a rainbow colors the text glyph by glyph", markup.copy().effects(DemoRainbowTextEffect.inst()))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Rainbow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 780, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("scrambled characters keep their width", markup.copy().effects(DemoScrambleTextEffect.inst()))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Scramble", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 780, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 30, 280, 0).text(Text.create("the shadow waves with the text", markup.copy().effects(DemoWaveTextEffect.inst()).shadow(Color.BLACK))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Wave and shadow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 780, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>markup</b> <u>adds</u> <h>effects</h> under the rainbow", markup.copy().effects(DemoRainbowTextEffect.inst()))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Effect and markup", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}