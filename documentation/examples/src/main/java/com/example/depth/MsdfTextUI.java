package com.example.depth;

import com.example.ExampleFont;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class MsdfTextUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(310, 260, 1300, 560)
		.body(card -> {
			super.box(40, 60, 220, 120, "Font file", ".ttf, .otf, .ttc").attach(card);
			super.box(320, 60, 260, 120, "Cache folder", "an atlas of these bytes?").attach(card);
			super.subject(640, 60, 260, 120, "Generation", "only when it is missing").attach(card);
			super.box(960, 60, 300, 120, "Atlas", "2048 x 2048 distance field").attach(card);
			super.arrow(card, 262D, 120D, 318D, 120D);
			super.arrow(card, 582D, 120D, 638D, 120D);
			super.arrow(card, 902D, 120D, 958D, 120D);
			super.arrow(card, 1110D, 182D, 1110D, 258D);
			super.box(780, 260, 480, 120, "Shader", "the edge of each screen pixel").attach(card);
			super.label(40, 260, 700, 40, "One atlas draws every size:", Align.START).attach(card);
			TextNode.create(40, 470).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 24F).color(DepthDiagramUI.LABEL))).anchorY(Align.END).attach(card);
			TextNode.create(120, 470).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 48F).color(DepthDiagramUI.LABEL))).anchorY(Align.END).attach(card);
			TextNode.create(260, 470).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 96F).color(DepthDiagramUI.LABEL))).anchorY(Align.END).attach(card);
			TextNode.create(500, 470).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 160F).color(DepthDiagramUI.ACCENT))).anchorY(Align.END).attach(card);
		})
		.attach(this);
	}

}