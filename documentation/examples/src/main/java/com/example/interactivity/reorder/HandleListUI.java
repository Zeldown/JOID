package com.example.interactivity.reorder;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class HandleListUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(HandleListUI.TEXT);
		final TextInfo dark = TextInfo.create(ExampleFont.REGULAR, 24F).color(HandleListUI.ON_ACCENT);

		RectNode
		.create(660, 290, 600, 500)
		.color(HandleListUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(HandleListUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region handle
			final ReorderableFlexNode list = ReorderableFlexNode.vertical(60, 40, 480).autoDrag(false).margin(10D).attach(card);
			final RectNode header = RectNode
			.create(0, 0, 480, 70)
			.color(HandleListUI.ACCENT)
			.label(Text.create("To do", dark))
			.attach(list);

			for (final String task : new String[] {"Write", "Review", "Ship"}) {
				RectNode
				.create(0, 0, 480, 70)
				.color(HandleListUI.ELEMENT)
				.label(Text.create(task, info))
				.body(item -> {
					RectNode
					.create(420, 15, 40, 40)
					.color(HandleListUI.ACCENT)
					.cursor(Cursor.MOVE)
					.handleMousePressed(event -> {
						if (event.getNode().isHovered() && event.getButton().isLeft()) {
							list.startDrag(item);
							event.consume();
						}
					})
					.attach(item);
				})
				.attach(list);
			}
			// #region pin
			list.pin(header);
			// #endregion
			// #endregion
		})
		.attach(this);
	}

}