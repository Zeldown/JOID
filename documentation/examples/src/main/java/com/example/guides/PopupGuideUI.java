package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class PopupGuideUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(PopupGuideUI.TEXT);
		// #region open
		final StringSignal file = new StringSignal("report.pdf");

		RectNode
		.create(560, 290, 800, 500)
		.color(PopupGuideUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PopupGuideUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(400, 150).text(Text.create(file.get(), info)).anchor(Align.CENTER).attach(card);
			RectNode
			.create(300, 380, 200, 70)
			.color(PopupGuideUI.ACCENT)
			.label(Text.create("Delete", info.withColor(PopupGuideUI.ON_ACCENT)))
			.onClick(event -> JOID.open(new PopupGuideConfirmPopup(() -> file.set("No file"))))
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}