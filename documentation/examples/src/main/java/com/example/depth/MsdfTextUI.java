package com.example.depth;

import com.example.ExampleFont;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class MsdfTextUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(510, 105, 900, 870)
		.body(card -> {
			super.box(40, 40, 380, 110, "Font file", ".ttf, .otf, .ttc").attach(card);
			super.box(480, 40, 380, 110, "Cache folder", "an atlas of these bytes?").attach(card);
			super.subject(480, 250, 380, 110, "Generation", "only when it is missing").attach(card);
			super.box(40, 250, 380, 110, "Atlas", "2048 x 2048 distance field").attach(card);
			super.box(40, 460, 380, 110, "Shader", "the edge of each screen pixel").attach(card);
			super.arrow(card, 422D, 95D, 478D, 95D);
			super.arrow(card, 670D, 152D, 670D, 248D);
			super.arrow(card, 520D, 152D, 380D, 248D);
			super.arrow(card, 478D, 305D, 422D, 305D);
			super.arrow(card, 230D, 362D, 230D, 458D);
			super.label(684, 182, 170, 36, "missing", Align.START).attach(card);
			super.label(476, 190, 150, 36, "found", Align.START).attach(card);
			super.label(40, 620, 820, 40, "One atlas draws every size:", Align.START).attach(card);
			TextNode.create(40, 830).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 24F).color(DepthDiagramUI.LABEL))).anchorY(Align.END).attach(card);
			TextNode.create(120, 830).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 48F).color(DepthDiagramUI.LABEL))).anchorY(Align.END).attach(card);
			TextNode.create(260, 830).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 96F).color(DepthDiagramUI.LABEL))).anchorY(Align.END).attach(card);
			TextNode.create(500, 830).text(Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, 160F).color(DepthDiagramUI.ACCENT))).anchorY(Align.END).attach(card);
		})
		.attach(this);
	}

}