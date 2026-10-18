package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.font.effect.DemoHighlightTextEffect;
import dev.joid.demo.font.effect.DemoRainbowTextEffect;
import dev.joid.demo.font.effect.DemoScrambleTextEffect;
import dev.joid.demo.font.effect.DemoUnderlineTextEffect;
import dev.joid.demo.font.effect.DemoWaveTextEffect;
import dev.joid.demo.font.markup.DemoTextMarkup;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.TextOverflow;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoMarkup extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoMarkup.INK);
		final TextInfo markup = TextInfo.create(DemoFont.MONTSERRAT, 20).color(UIDemoMarkup.INK).markups(DemoTextMarkup.inst());
		final int[] slots = {0, 1, 2, 3, 5, 6, 7, 8, 10, 12, 13, 14, 15, 16, 17, 18, 19};
		final Text[] texts = {
			Text.create("<b>bold</b> regular <w=100>thin</w> <w=300>light</w> <w=600>semi bold</w> <w=900>black</w> <w=800>extra bold</w>", markup),
			Text.create("<i>italic</i> upright <b><i>bold italic</i></b> <w=100><i>thin italic</i></w>", markup),
			Text.create("<c=cc3333>red</c> <c=2e8b57>green</c> <c=3355cc>blue</c> <c=e67e00>orange <b>bold</b> still orange</c> ink", markup),
			Text.create("<u>underline</u> <h>highlight</h> <u><h>both</h></u> <c=cc3333><u>colored underline</u></c> <b><u>bold underline</u></b>", markup),
			Text.create("<c=3355cc>the shadow <b>follows</b> the <u>markup</u></c> <h>behind the highlight</h>", markup.copy().shadow(Color.BLACK)),
			Text.create("<c=cc3333>colors</c> <c=2e8b57>are</c> <b>ignored</b> when the text is not colored", markup.copy().colored(false)),
			Text.create("<b>raw</b> <c=cc3333>tags</c> <u>stay</u> text", markup.copy().markups()),
			Text.create("<b>spaced</b> <c=3355cc>markup</c> <u>keeps</u> its <h>spacing</h>", markup.copy().letterSpacing(0.3F)),
			Text.create("<b>Bold words</b> and <c=cc3333>a colored phrase that wraps over several lines</c> keep their style", markup),
			Text.create("<i>italic</i> <c=cc3333>red</c> <u>underline</u> <h>highlight</h>", markup.copy().font(DemoFont.PACIFICO)),
			Text.create("<i>italic</i> <c=cc3333>red</c> <u>underline</u> <h>highlight</h>", markup.copy().font(DemoFont.PLAYFAIR_DISPLAY)),
			Text.create("an underline and a highlight on the whole text", markup.copy().effects(DemoUnderlineTextEffect.inst(), DemoHighlightTextEffect.inst())),
			Text.create("a wave moves every glyph without touching the layout", markup.copy().effects(DemoWaveTextEffect.inst())),
			Text.create("a rainbow colors the text glyph by glyph", markup.copy().effects(DemoRainbowTextEffect.inst())),
			Text.create("scrambled characters keep their width", markup.copy().effects(DemoScrambleTextEffect.inst())),
			Text.create("the shadow waves with the text", markup.copy().effects(DemoWaveTextEffect.inst()).shadow(Color.BLACK)),
			Text.create("<b>markup</b> <u>adds</u> <h>effects</h> under the rainbow", markup.copy().effects(DemoRainbowTextEffect.inst()))
		};
		final String[] captions = {"Weights", "Italic", "Colors", "Decorations", "Shadow", "Uncolored", "Markup off", "Spaced", "Wrapped", "Pacifico", "Playfair Display", "Whole text", "Wave", "Rainbow", "Scramble", "Wave and shadow", "Effect and markup"};

		for (int i = 0; i < slots.length; i++) {
			final int index = i;
			RectNode
			.create(80 + slots[index] % 5 * 360, 30 + slots[index] / 5 * 250, 320, 190)
			.color(UIDemoMarkup.PLACEHOLDER)
			.body(rect -> {
				TextNode.create(20, 20, 280, 0).text(texts[index]).mode(TextMode.SPLIT).attach(rect);
				TextNode.create(160, 205).text(Text.create(captions[index], caption)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(1520, 30, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>bold <i>bold italic <c=3355cc>blue <u>underlined</u></c></i></b> back to ink", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(20, 110, 280, 0).text(Text.create("<f=pacifico>Pacifico <c=3355cc>blue</c></f> <f=playfair><i>Playfair</i></f> ink", markup)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(160, 205).text(Text.create("Tags and fonts", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 280, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>ends</b> <c=3355cc>here</c>", markup).alignX(Align.END)).attach(rect);
			TextNode.create(20, 80, 280, 0).text(Text.create("<b>centered</b> <i>markup</i>", markup).alignX(Align.CENTER)).attach(rect);
			TextNode.create(160, 205).text(Text.create("Aligned", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 530, 320, 190)
		.color(UIDemoMarkup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 280, 0).text(Text.create("<b>bold</b> and <c=cc3333>red text cut by the width</c>", markup).overflow(TextOverflow.ELLIPSIS)).mode(TextMode.OVERFLOW).attach(rect);
			TextNode.create(160, 205).text(Text.create("Cut", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}