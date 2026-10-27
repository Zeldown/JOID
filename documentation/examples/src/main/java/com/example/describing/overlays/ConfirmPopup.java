package com.example.describing.overlays;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

// #region class
@UIData(background = true)
@UIDataPopup(active = true)
public class ConfirmPopup extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 32F).color(ConfirmPopup.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 26F).color(Color.WHITE);

		RectNode
		.create(710, 390, 500, 300)
		.color(ConfirmPopup.PLACEHOLDER)
		.body(card -> {
			TextNode.create(250, 90).text(Text.create("Quit the game?", title)).anchor(Align.CENTER).attach(card);
			RectNode
			.create(50, 190, 180, 70)
			.color(ConfirmPopup.INK)
			.label(Text.create("Cancel", label))
			.onClick(event -> JOID.close(this))
			.attach(card);
			RectNode
			.create(270, 190, 180, 70)
			.color(ConfirmPopup.INK)
			.label(Text.create("Quit", label))
			.onClick(event -> JOID.close(this))
			.attach(card);
		})
		.attach(this);
	}

}
// #endregion