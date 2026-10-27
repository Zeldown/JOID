package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableType;

public class CopyUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo white = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		RectNode
		.create(560, 340, 800, 400)
		.color(CopyUI.PLACEHOLDER)
		.body(card -> {
			// #region copy
			final RectNode slot = RectNode.create(480, 140, 240, 120).color(Color.WHITE).attach(card);

			RectNode
			.create(80, 160, 160, 80)
			.color(CopyUI.INK)
			.label(Text.create("Sword", white))
			.draggable(DraggableProperty.parent().type(DraggableType.COPY).snapType(DraggableSnapType.OVERLAP).snap(slot))
			.onDragSnap(event -> {
				event.getTarget().clearChildren();
				RectNode.create(40, 20, 160, 80).color(CopyUI.INK).label(Text.create("Sword", white)).attach(event.getTarget());
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}