package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.font.effect.DemoUnderlineTextEffect;
import dev.joid.demo.font.effect.DemoWaveTextEffect;
import dev.joid.demo.font.markup.DemoTextMarkup;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoFont extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private static final MsdfFont LIGHT_BOLD = MsdfFont.of(DemoFont.MONTSERRAT.getFace(FontWeight.LIGHT, false), DemoFont.MONTSERRAT.getFace(FontWeight.BOLD, false));

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoFont.INK);
		final TextInfo montserrat = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoFont.INK).lineHeight(1.33F);
		final TextInfo pacifico = TextInfo.create(DemoFont.PACIFICO, 18, UIDemoFont.INK).lineHeight(1.425F);
		final TextInfo playfair = TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 18, UIDemoFont.INK).lineHeight(1.425F);
		final TextInfo pixel = TextInfo.create(DemoFont.PIXEL, 16, UIDemoFont.INK);
		final String[] specimen = {"Aa Bb Cc Dd Ee Ff Gg", "abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLM", "NOPQRSTUVWXYZ", "0123456789", "!?.,;:'\"()[]{}+-*/=%&@#", "àâéèêëîïôùûüç ß œ æ ł ı", "ÀÉÈÊÎÔÙÇ"};
		final TextInfo[] specimens = {montserrat, pacifico, playfair};
		final String[] names = {"Montserrat", "Pacifico", "Playfair Display"};
		final boolean[] italics = {false, true};
		final int[][] weightPositions = {{1520, 40}, {80, 380}};
		final String[] weightCaptions = {"Weights", "Italic weights"};

		for (int i = 0; i < specimens.length; i++) {
			final int index = i;
			RectNode
			.create(80 + index * 360, 40, 320, 260)
			.color(UIDemoFont.PLACEHOLDER)
			.body(rect -> {
				FlexNode
				.vertical(20, 20, 280)
				.body(flex -> {
					for (final String line : specimen) {
						TextNode.create(0, 0).text(Text.create(line, specimens[index])).attach(flex);
					}
				})
				.attach(rect);
				TextNode.create(160, 275).text(Text.create(names[index], caption)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(6D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("AVATAR Tower WAVE", montserrat.copy().fontSize(24F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("AVATAR Tower WAVE", pacifico.copy().fontSize(24F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("AVATAR Tower WAVE", playfair.copy().fontSize(24F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("LT Ty Yo Va", montserrat.copy().fontSize(40F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Kerning", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		for (int i = 0; i < italics.length; i++) {
			final int index = i;
			RectNode
			.create(weightPositions[index][0], weightPositions[index][1], 320, 260)
			.color(UIDemoFont.PLACEHOLDER)
			.body(rect -> {
				FlexNode
				.vertical(20, 20, 280)
				.body(flex -> {
					for (final FontWeight weight : FontWeight.values()) {
						TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, weight, 18, UIDemoFont.INK).italic(italics[index]))).attach(flex);
					}
				})
				.attach(rect);
				TextNode.create(160, 275).text(Text.create(weightCaptions[index], caption)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.body(flex -> {
				for (final FontWeight weight : FontWeight.values()) {
					TextNode.create(0, 0).text(Text.create(weight.getValue() + " drawn with two faces", TextInfo.create(UIDemoFont.LIGHT_BOLD, weight, 18, UIDemoFont.INK))).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Nearest weight", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(4D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Size 12", montserrat.copy().fontSize(12F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 16", montserrat.copy().fontSize(16F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 24", montserrat.copy().fontSize(24F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 32", montserrat.copy().fontSize(32F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 48", montserrat.copy().fontSize(48F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Sizes", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(10D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Montserrat italic", montserrat.copy().fontSize(28F).italic(true))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Pacifico italic", pacifico.copy().fontSize(28F).italic(true))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Playfair italic", playfair.copy().fontSize(28F).italic(true))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Italic", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(6D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Size 8: the quick brown fox jumps", montserrat.copy().fontSize(8F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 9: the quick brown fox jumps", montserrat.copy().fontSize(9F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 10: the quick brown fox jumps", montserrat.copy().fontSize(10F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 11: the quick brown fox jumps", montserrat.copy().fontSize(11F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 10: the quick brown fox jumps", playfair.copy().fontSize(10F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 10: the quick brown fox jumps", pacifico.copy().fontSize(10F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Small sizes", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(160, 130).text(Text.create("Ag", montserrat.copy().fontSize(160F)).align(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(160, 275).text(Text.create("Large size", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(10D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("White text", montserrat.copy().fontSize(28F).color(Color.WHITE))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Ink text", montserrat.copy().fontSize(28F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Half transparent", montserrat.copy().fontSize(28F).color(Color.BLACK.withAlpha(0.5F)))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Black text", montserrat.copy().fontSize(28F).color(Color.BLACK))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Colors", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(6D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("THE QUICK BROWN FOX", pixel)).attach(flex);
				TextNode.create(0, 0).text(Text.create("jumps over the lazy dog", pixel)).attach(flex);
				TextNode.create(0, 0).text(Text.create("0123456789", pixel)).attach(flex);
				TextNode.create(0, 0).text(Text.create("!?.,;:'\"()[]{}+-*/=%&@#", pixel)).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Pixel font", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(4D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Size 8", pixel.copy().fontSize(8F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 12", pixel.copy().fontSize(12F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 16", pixel.copy().fontSize(16F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 20", pixel.copy().fontSize(20F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 32", pixel.copy().fontSize(32F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Pixel sizes", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(10D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Shadow", pixel.copy().color(Color.WHITE).shadow(Color.BLACK))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Underline", pixel.copy().effects(DemoUnderlineTextEffect.inst()))).attach(flex);
				TextNode.create(0, 0).text(Text.create("<c=cc3333>red</c> <c=3355cc>blue</c> ink", pixel.copy().markups(DemoTextMarkup.inst()))).attach(flex);
				TextNode.create(0, 0).text(Text.create("A wave of pixels", pixel.copy().effects(DemoWaveTextEffect.inst()))).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Pixel effects", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}