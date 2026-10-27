package com.example.describing.uis;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EditorUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	// #region class
	private boolean saved;

	@Override
	public void init() {
		this.saved = false;
		super.beforeClose(event -> {
			if (!this.saved) {
				event.cancel();
			}
		});

		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(Color.WHITE);

		RectNode
		.create(760, 340, 400, 400)
		.color(EditorUI.PLACEHOLDER)
		.body(card -> RectNode
			.create(100, 300, 200, 60)
			.color(EditorUI.INK)
			.label(Text.create("Save", info))
			.onClick(event -> this.saved = true)
			.attach(card))
		.attach(this);
	}
	// #endregion

}