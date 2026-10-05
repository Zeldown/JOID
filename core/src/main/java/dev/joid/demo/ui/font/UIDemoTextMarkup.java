package dev.joid.demo.ui.font;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.font.markup.DemoTextMarkup;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoTextMarkup extends UIDemo {

	private static final TextInfo INFO = TextInfo.create(DemoFont.MONTSERRAT, 34, Color.WHITE).markups(DemoTextMarkup.inst());

	@Override
	public void init() {
		FlexNode.vertical(10D, 10D, 1900D).margin(16D).body(flex -> {
			TextNode.create(0, 0).text(Text.create("<b>bold</b> regular <w=100>thin</w> <w=300>light</w> <w=600>semi bold</w> <w=900>black</w> <w=800>extra <b>bold</b> back</w>", UIDemoTextMarkup.INFO)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<i>italic</i> upright <b><i>bold italic</i></b> <w=100><i>thin italic</i></w>", UIDemoTextMarkup.INFO)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<c=ff5555>red</c> <c=55ff55>green</c> <c=5555ff>blue</c> <c=ffaa00>orange <b>bold</b> still orange</c> white", UIDemoTextMarkup.INFO)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<u>underline</u> <h>highlight</h> <u><h>both</h></u> <c=ff55ff><u>colored underline</u></c> <b><u>bold underline</u></b>", UIDemoTextMarkup.INFO)).attach(flex);
			TextNode.create(0, 0).text(Text.create("<c=55ffff>the shadow <b>follows</b> the <u>markup</u></c> <h>and keeps the highlight behind</h>", UIDemoTextMarkup.INFO.copy().shadow(Color.BLACK))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<c=ff5555>colors</c> <c=55ff55>are</c> <b>ignored</b> when the text is not colored", UIDemoTextMarkup.INFO.copy().colored(false))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>raw</b> <c=ff5555>markup</c> <u>when</u> markups are disabled", UIDemoTextMarkup.INFO.copy().markups())).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>spaced</b> <c=ffff55>markup</c> <u>keeps</u> its <h>spacing</h>", UIDemoTextMarkup.INFO.copy().letterSpacing(8F))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>measured</b> <w=900>with</w> <c=55ff55>markup</c> <u>ends here</u>", UIDemoTextMarkup.INFO, Align.END)).width(flex.getWidth()).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>centered</b> <w=100>with</w> <i>markup</i>", UIDemoTextMarkup.INFO, Align.CENTER)).width(flex.getWidth()).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>bold</b> <i>italic</i> <c=ff5555>red</c> <u>underline</u> <h>highlight</h>", UIDemoTextMarkup.INFO.copy().font(DemoFont.BATUPHAT))).attach(flex);
			TextNode.create(0, 0).text(Text.create("<b>bold</b> <i>italic</i> <c=ff5555>red</c> <u>underline</u> <h>highlight</h>", UIDemoTextMarkup.INFO.copy().font(DemoFont.SPACE_GROTESK))).attach(flex);
		}).attach(this);
	}

}