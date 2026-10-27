package com.example.guides;

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

// #region data
@UIData(background = true)
@UIDataPopup(active = true)
public class PopupGuideConfirmPopup extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final Runnable action;

	public PopupGuideConfirmPopup(final Runnable action) {
		this.action = action;
	}
	// #endregion

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 32F).color(PopupGuideConfirmPopup.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 26F).color(Color.WHITE);

		RectNode
		.create(710, 390, 500, 300)
		.color(PopupGuideConfirmPopup.PLACEHOLDER)
		.body(card -> {
			TextNode.create(250, 90).text(Text.create("Delete the file?", title)).anchor(Align.CENTER).attach(card);
			RectNode
			.create(50, 190, 180, 70)
			.color(PopupGuideConfirmPopup.INK)
			.label(Text.create("Cancel", label))
			.onClick(event -> JOID.close(this))
			.attach(card);
			// #region delete
			RectNode
			.create(270, 190, 180, 70)
			.color(PopupGuideConfirmPopup.INK)
			.label(Text.create("Delete", label))
			.onClick(event -> {
				this.action.run();
				JOID.close(this);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}