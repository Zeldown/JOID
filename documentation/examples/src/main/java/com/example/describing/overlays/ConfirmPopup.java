package com.example.describing.overlays;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

// #region class
@UIData(background = true)
@UIDataPopup(active = true)
public class ConfirmPopup extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 32F).color(ConfirmPopup.TEXT);
		final TextInfo cancel = TextInfo.create(ExampleFont.REGULAR, 26F).color(ConfirmPopup.TEXT);
		final TextInfo quit = TextInfo.create(ExampleFont.REGULAR, 26F).color(ConfirmPopup.ON_ACCENT);

		RectNode
		.create(710, 390, 500, 300)
		.color(ConfirmPopup.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ConfirmPopup.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(250, 90).text(Text.create("Quit the game?", title)).anchor(Align.CENTER).attach(card);
			RectNode
			.create(50, 190, 180, 70)
			.color(ConfirmPopup.ELEMENT)
			.label(Text.create("Cancel", cancel))
			.onClick(event -> JOID.close(this))
			.attach(card);
			RectNode
			.create(270, 190, 180, 70)
			.color(ConfirmPopup.ACCENT)
			.label(Text.create("Quit", quit))
			.onClick(event -> JOID.close(this))
			.attach(card);
		})
		.attach(this);
	}

}
// #endregion