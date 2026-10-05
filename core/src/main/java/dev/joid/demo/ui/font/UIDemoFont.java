package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoFont extends UIDemo {

	private static final TextInfo[] FONTS = {
			TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE).lineHeight(-5.5F),
			TextInfo.create(DemoFont.BATUPHAT, 25, Color.WHITE).lineHeight(-5.5F),
			TextInfo.create(DemoFont.SPACE_GROTESK, 25, Color.WHITE)
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
			FlexNode.vertical(10D + i * 640D, 10D, 620D).margin(5.8D).body(flex -> {
				for (final String text : UIDemoFont.TEXTS) {
					final boolean italic = text.equals("italic");
					final boolean spacing = text.contains("spacing");
					final boolean negativeSpacing = text.contains("n-spacing");
					final boolean hasShadow = text.contains("shadow");
					final boolean hasColoredShadow = text.contains("colored");
					final boolean split = text.contains("splitted");
					final boolean overflow = text.contains("overflow");
					TextNode.create(0, 0).text(Text.create(text, info.copy().italic(italic).letterSpacing(negativeSpacing ? -4F : spacing ? 10F : 0F).shadow(hasShadow ? hasColoredShadow ? Color.RAINBOW() : Color.BLACK : null)).overflow(overflow ? TextOverflow.ELLIPSIS : TextOverflow.NONE).horizontalAlign(align)).mode(split ? TextMode.SPLIT : overflow ? TextMode.OVERFLOW : TextMode.NORMAL).width(flex.getWidth()).attach(flex);
				}
			}).attach(this);
		}
	}

}