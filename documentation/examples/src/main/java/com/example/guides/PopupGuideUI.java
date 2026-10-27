package com.example.guides;

import com.example.ExampleFont;

import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class PopupGuideUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(PopupGuideUI.INK);
		// #region open
		final StringSignal file = new StringSignal("report.pdf");

		RectNode
		.create(560, 290, 800, 500)
		.color(Color.WHITE)
		.body(card -> {
			TextNode.create(400, 150).text(Text.create(file.get(), info)).anchor(Align.CENTER).attach(card);
			RectNode
			.create(300, 380, 200, 70)
			.color(PopupGuideUI.INK)
			.label(Text.create("Delete", info.withColor(Color.WHITE)))
			.onClick(event -> JOID.open(new PopupGuideConfirmPopup(() -> file.set("No file"))))
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}