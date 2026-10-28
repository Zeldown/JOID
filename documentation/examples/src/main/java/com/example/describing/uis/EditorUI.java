package com.example.describing.uis;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EditorUI extends UI {

	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

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

		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(EditorUI.ON_ACCENT);

		RectNode
		.create(760, 340, 400, 400)
		.color(EditorUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(EditorUI.ELEMENT, 1.5F))
		.body(card -> RectNode
			.create(100, 300, 200, 60)
			.color(EditorUI.ACCENT)
			.label(Text.create("Save", info))
			.onClick(event -> this.saved = true)
			.attach(card))
		.attach(this);
	}
	// #endregion

}