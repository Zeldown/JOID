package com.example.guides;

import com.example.ExampleFont;

import dev.joid.internal.JOID;
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
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(PopupGuideConfirmPopup.INK);

		// #region card
		RectNode
		.create(710, 390, 500, 300)
		.color(PopupGuideConfirmPopup.PLACEHOLDER)
		.body(card -> {
			TextNode.create(250, 90).text(Text.create("Delete the file?", info)).anchor(Align.CENTER).attach(card);
			RectNode
			.create(40, 190, 200, 70)
			.color(Color.WHITE)
			.label(Text.create("Cancel", info))
			.onClick(event -> JOID.close(this))
			.attach(card);
			RectNode
			.create(260, 190, 200, 70)
			.color(Color.WHITE)
			.label(Text.create("Delete", info))
			.onClick(event -> {
				this.action.run();
				JOID.close(this);
			})
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}