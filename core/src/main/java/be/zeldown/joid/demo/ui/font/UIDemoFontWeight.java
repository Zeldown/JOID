package be.zeldown.joid.demo.ui.font;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.font.impl.msdf.MsdfFont;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;

public class UIDemoFontWeight extends UIDemo {

	private static final MsdfFont LIGHT_BOLD = MsdfFont.create(DemoFont.MONTSERRAT.getFace(FontWeight.LIGHT, false), DemoFont.MONTSERRAT.getFace(FontWeight.BOLD, false));

	@Override
	public void init() {
		FlexNode.vertical(10D, 10D, 460D).margin(14D).body(flex -> {
			for (final FontWeight weight : FontWeight.values()) {
				TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, weight, 40, Color.WHITE))).attach(flex);
			}
		}).attach(this);
		FlexNode.vertical(490D, 10D, 460D).margin(14D).body(flex -> {
			for (final FontWeight weight : FontWeight.values()) {
				TextNode.create(0, 0).text(Text.create(weight.getValue() + " " + weight, TextInfo.create(DemoFont.MONTSERRAT, 40, Color.WHITE).weight(weight).italic(true))).attach(flex);
			}
		}).attach(this);
		FlexNode.vertical(970D, 10D, 460D).margin(14D).body(flex -> {
			for (int value = 100; value <= 900; value += 100) {
				TextNode.create(0, 0).text(Text.create(value + " " + FontWeight.of(value), TextInfo.create(UIDemoFontWeight.LIGHT_BOLD, FontWeight.of(value), 40, Color.WHITE))).attach(flex);
			}
		}).attach(this);
		FlexNode.vertical(1450D, 10D, 460D).margin(14D).body(flex -> {
			for (int value = 100; value <= 900; value += 100) {
				TextNode.create(0, 0).text(Text.create(value + " " + FontWeight.of(value), TextInfo.create(UIDemoFontWeight.LIGHT_BOLD, FontWeight.of(value), 40, Color.WHITE).italic(true).shadow(Color.BLACK))).attach(flex);
			}
		}).attach(this);
	}

}