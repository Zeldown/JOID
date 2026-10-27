package com.example.interactivity.reorder;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class HandleListUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(HandleListUI.INK);
		final TextInfo white = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		RectNode
		.create(660, 290, 600, 500)
		.color(HandleListUI.PLACEHOLDER)
		.body(card -> {
			// #region handle
			final ReorderableFlexNode list = ReorderableFlexNode.vertical(60, 40, 480).autoDrag(false).margin(10D).attach(card);
			final RectNode header = RectNode
			.create(0, 0, 480, 70)
			.color(HandleListUI.INK)
			.label(Text.create("To do", white))
			.attach(list);

			for (final String task : new String[] {"Write", "Review", "Ship"}) {
				RectNode
				.create(0, 0, 480, 70)
				.color(Color.WHITE)
				.label(Text.create(task, info))
				.body(item -> {
					RectNode
					.create(420, 15, 40, 40)
					.color(HandleListUI.INK)
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