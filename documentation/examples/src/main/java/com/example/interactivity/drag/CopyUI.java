package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableType;

public class CopyUI extends UI {

	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo dark = TextInfo.create(ExampleFont.REGULAR, 24F).color(CopyUI.ON_ACCENT);

		RectNode
		.create(560, 340, 800, 400)
		.color(CopyUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CopyUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region copy
			final RectNode slot = RectNode.create(480, 140, 240, 120).color(CopyUI.ELEMENT).attach(card);

			RectNode
			.create(80, 160, 160, 80)
			.color(CopyUI.ACCENT)
			.label(Text.create("Sword", dark))
			.draggable(DraggableProperty.parent().type(DraggableType.COPY).snapType(DraggableSnapType.OVERLAP).snap(slot))
			.onDragSnap(event -> {
				event.getTarget().clearChildren();
				RectNode.create(40, 20, 160, 80).color(CopyUI.ACCENT).label(Text.create("Sword", dark)).attach(event.getTarget());
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}