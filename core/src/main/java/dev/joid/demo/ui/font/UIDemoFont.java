package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoFont extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	private static final MsdfFont LIGHT_BOLD = MsdfFont.create(DemoFont.MONTSERRAT.getFace(FontWeight.LIGHT, false), DemoFont.MONTSERRAT.getFace(FontWeight.BOLD, false));

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoFont.INK);
		final TextInfo montserrat = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoFont.INK).lineHeight(1.33F);
		final TextInfo pacifico = TextInfo.create(DemoFont.PACIFICO, 18, UIDemoFont.INK).lineHeight(1.425F);
		final TextInfo playfair = TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 18, UIDemoFont.INK).lineHeight(1.425F);
		final String[] specimen = {"Aa Bb Cc Dd Ee Ff Gg", "abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLM", "NOPQRSTUVWXYZ", "0123456789", "!?.,;:'\"()[]{}+-*/=%&@#", "àâéèêëîïôùûüç ß œ æ ł ı", "ÀÉÈÊÎÔÙÇ"};

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (final String line : specimen) {
					TextNode.create(0, 0).text(Text.create(line, montserrat)).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Montserrat", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (final String line : specimen) {
					TextNode.create(0, 0).text(Text.create(line, pacifico)).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Pacifico", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (final String line : specimen) {
					TextNode.create(0, 0).text(Text.create(line, playfair)).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Playfair Display", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.margin(6D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("AVATAR Tower WAVE", montserrat.copy().fontSize(28F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("AVATAR Tower WAVE", pacifico.copy().fontSize(28F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("AVATAR Tower WAVE", playfair.copy().fontSize(28F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("LT Ty Yo Va", montserrat.copy().fontSize(40F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Kerning", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (final FontWeight weight : FontWeight.values()) {
					TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, weight, 18, UIDemoFont.INK))).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Weights", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (final FontWeight weight : FontWeight.values()) {
					TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, weight, 18, UIDemoFont.INK).italic(true))).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Italic weights", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (final FontWeight weight : FontWeight.values()) {
					TextNode.create(0, 0).text(Text.create(weight.getValue() + " drawn with two faces", TextInfo.create(UIDemoFont.LIGHT_BOLD, weight, 18, UIDemoFont.INK))).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Nearest weight", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.margin(4D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Size 12", montserrat.copy().fontSize(12F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 16", montserrat.copy().fontSize(16F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 24", montserrat.copy().fontSize(24F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 32", montserrat.copy().fontSize(32F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Size 48", montserrat.copy().fontSize(48F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Sizes", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Montserrat italic", montserrat.copy().fontSize(28F).italic(true))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Pacifico italic", pacifico.copy().fontSize(28F).italic(true))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Playfair italic", playfair.copy().fontSize(28F).italic(true))).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Italic", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
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
			TextNode.create(200, 275).text(Text.create("Small sizes", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 130).text(Text.create("Ag", montserrat.copy().fontSize(160F), Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 275).text(Text.create("Large size", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoFont.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("White text", montserrat.copy().fontSize(28F).color(Color.WHITE))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Ink text", montserrat.copy().fontSize(28F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Half transparent", montserrat.copy().fontSize(28F).color(Color.BLACK.copyAlpha(0.5F)))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Black text", montserrat.copy().fontSize(28F).color(Color.BLACK))).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Colors", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}