package be.zeldown.joid.demo.ui.font;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.draw.text.builder.utils.TextOverflow;
import be.zeldown.joid.lib.draw.text.utils.TextMode;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.utils.align.Align;

public class UIDemoFont extends UIDemo {

	private static final TextInfo[] FONTS = {
			TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE).lineHeight(-5.5F),
			TextInfo.create(DemoFont.BATUPHAT, 25, Color.WHITE).lineHeight(-5.5F),
			TextInfo.create(DemoFont.SPACE_GROTESK, 25, Color.WHITE)
	};

	private static final String[] TEXTS = {
			"lorem impsum",
			"§oitalic",
			"spacing",
			"n-spacing",
			"§00 §11 §22 §33 §44 §55 §66 §77 §88 §99 §aa §bb §cc §dd §ee §ff §pp",
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
			"splitted text splitted text splitted text splitted text splitted splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted text splitted textsplitted text splitted text splitted text splitted text splitted text",
			"overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow overflow",
			"colored shadow text",
			"§cshadow §ltext"
	};

	@Override
	public void init() {
		FlexNode.vertical(10D, 10D, 1900D).margin(5.8D).body(node -> {
			for (int i = 0; i < UIDemoFont.FONTS.length; i++) {
				final TextInfo info = UIDemoFont.FONTS[i];
				final Align align = i == 0 ? Align.START : i == 1 ? Align.CENTER : Align.END;
				for (final String text : UIDemoFont.TEXTS) {
					final boolean spacing = text.contains("spacing");
					final boolean negativeSpacing = text.contains("n-spacing");
					final boolean hasShadow = text.contains("shadow");
					final boolean hasColoredShadow = text.contains("colored");
					final boolean split = text.contains("splitted");
					final boolean overflow = text.contains("overflow");
					TextNode.create(0, 0).text(Text.create(text, info.copy().letterSpacing(negativeSpacing ? -4F : spacing ? 10F : 0F).shadow(hasShadow ? hasColoredShadow ? Color.RAINBOW() : Color.BLACK : null)).overflow(overflow ? TextOverflow.ELLIPSIS : TextOverflow.NONE).horizontalAlign(align)).mode(split ? TextMode.SPLIT : overflow ? TextMode.OVERFLOW : TextMode.NORMAL).width(node.getWidth()).attach(node);
				}
			}
		}).attach(this);
	}

}