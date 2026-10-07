package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.TextElement;
import dev.joid.lib.draw.text.builder.modifier.TextModifier;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoText extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoText.INK);
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoText.INK);
		final String paragraph = "A long sentence wraps on the width of its node, word after word, and keeps its alignment on every line.";

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 30, 360, 50)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 360, 50).text(Text.create("Start", caption, Align.START, Align.CENTER)).attach(box);
			})
			.attach(rect);
			RectNode
			.create(20, 100, 360, 50)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 360, 50).text(Text.create("Center", caption, Align.CENTER, Align.CENTER)).attach(box);
			})
			.attach(rect);
			RectNode
			.create(20, 170, 360, 50)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 360, 50).text(Text.create("End", caption, Align.END, Align.CENTER)).attach(box);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Horizontal align", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 30, 110, 200)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 110, 200).text(Text.create("Top", caption, Align.CENTER, Align.START)).attach(box);
			})
			.attach(rect);
			RectNode
			.create(145, 30, 110, 200)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 110, 200).text(Text.create("Middle", caption, Align.CENTER, Align.CENTER)).attach(box);
			})
			.attach(rect);
			RectNode
			.create(270, 30, 110, 200)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 110, 200).text(Text.create("Bottom", caption, Align.CENTER, Align.END)).attach(box);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Vertical align", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 30, 360, 0).text(Text.create(paragraph, info)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(20, 140, 360, 0).text(Text.create(paragraph, info, Align.CENTER)).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(200, 275).text(Text.create("Split", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 60, 360, 46)
			.color(Color.WHITE)
			.body(box -> {
				TextNode.create(0, 0, 360, 46).text(Text.create(paragraph, info)).mode(TextMode.BOX).attach(box);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Box", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 30, 360)
			.margin(20D)
			.body(flex -> {
				TextNode.create(0, 0, 360, 0).text(Text.create("None cuts this sentence where the node ends", info, TextOverflow.NONE)).mode(TextMode.OVERFLOW).attach(flex);
				TextNode.create(0, 0, 360, 0).text(Text.create("Ellipsis cuts this sentence where the node ends", info, TextOverflow.ELLIPSIS)).mode(TextMode.OVERFLOW).attach(flex);
				TextNode.create(0, 0, 360, 0).text(Text.create("Dot cuts this sentence where the node ends", info, TextOverflow.DOT)).mode(TextMode.OVERFLOW).attach(flex);
				TextNode.create(0, 0, 360, 0).text(Text.create("Hyphen cuts this sentence where the node ends", info, TextOverflow.HYPHEN)).mode(TextMode.OVERFLOW).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Overflow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 30, 360)
			.margin(20D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Spacing 0.3", caption.copy().letterSpacing(0.3F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Spacing 0.1", caption.copy().letterSpacing(0.1F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Spacing 0", caption)).attach(flex);
				TextNode.create(0, 0).text(Text.create("Spacing -0.05", caption.copy().letterSpacing(-0.05F))).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Letter spacing", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(20, 20, 360, 0).text(Text.create("A line height of 1 keeps the lines of a paragraph close", info.copy().lineHeight(1F))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(20, 110, 360, 0).text(Text.create("A line height of 1.8 opens the lines of a paragraph", info.copy().lineHeight(1.8F))).mode(TextMode.SPLIT).attach(rect);
			TextNode.create(200, 275).text(Text.create("Line height", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 30, 360)
			.margin(20D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("Default shadow", caption.copy().shadow())).attach(flex);
				TextNode.create(0, 0).text(Text.create("White shadow", caption.copy().shadow(Color.WHITE))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Offset shadow", caption.copy().shadow(Color.BLACK.copyAlpha(0.4F)).shadow(4F, 4F))).attach(flex);
				TextNode.create(0, 0).text(Text.create("Rainbow shadow", caption.copy().shadow(Color.RAINBOW))).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Shadow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 360)
			.margin(4D)
			.body(flex -> {
				TextNode.create(0, 0).text(Text.create("hello joid world", info).modifier(TextModifier.UPPER_CASE)).attach(flex);
				TextNode.create(0, 0).text(Text.create("HELLO JOID WORLD", info).modifier(TextModifier.LOWER_CASE)).attach(flex);
				TextNode.create(0, 0).text(Text.create("hello joid world", info).modifier(TextModifier.CAPITALIZE)).attach(flex);
				TextNode.create(0, 0).text(Text.create("hello joid world", info).modifier(TextModifier.WORD_CAPITALIZE)).attach(flex);
				TextNode.create(0, 0).text(Text.create("hello joid world", info).modifier(TextModifier.CAMEL_CASE)).attach(flex);
				TextNode.create(0, 0).text(Text.create("hello joid world", info).modifier(TextModifier.UPPER_CAMEL_CASE)).attach(flex);
				TextNode.create(0, 0).text(Text.create("hello joid world", info).modifier(TextModifier.SNAKE_CASE)).attach(flex);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Modifiers", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			TextNode
			.create(20, 40)
			.text(Text.create(TextElement.create("Several ", caption), TextElement.create("elements ", caption.copy().weight(FontWeight.BOLD)), TextElement.create("in one ", caption.copy().font(DemoFont.PLAYFAIR_DISPLAY)), TextElement.create("text", caption.copy().color(Color.WHITE))))
			.attach(rect);
			TextNode
			.create(20, 110, 360, 0)
			.text(Text.create(TextElement.create("They wrap ", info), TextElement.create("together ", info.copy().weight(FontWeight.BOLD)), TextElement.create("on the width of the node, ", info.copy().italic(true)), TextElement.create("element after element.", info)))
			.mode(TextMode.SPLIT)
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Text elements", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			TextNode
			.create(20, 40)
			.text(Text.create("Short", caption))
			.self(text -> text.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawFilledBorder(text.getX(), text.getY(), text.getX() + text.getWidth(), text.getY() + text.getHeight(), UIDemoText.INK, 1D)))
			.attach(rect);
			TextNode
			.create(20, 100)
			.text(Text.create("A longer text", caption.copy().fontSize(36F)))
			.self(text -> text.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawFilledBorder(text.getX(), text.getY(), text.getX() + text.getWidth(), text.getY() + text.getHeight(), UIDemoText.INK, 1D)))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Size from text", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoText.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(199, 20, 2, 220).color(Color.WHITE).attach(rect);
			TextNode.create(200, 40).text(Text.create("Anchor start", caption)).anchorX(Align.START).attach(rect);
			TextNode.create(200, 110).text(Text.create("Anchor center", caption)).anchorX(Align.CENTER).attach(rect);
			TextNode.create(200, 180).text(Text.create("Anchor end", caption)).anchorX(Align.END).attach(rect);
			TextNode.create(200, 275).text(Text.create("Anchors", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}