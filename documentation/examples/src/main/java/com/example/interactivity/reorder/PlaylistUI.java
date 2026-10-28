package com.example.interactivity.reorder;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class PlaylistUI extends UI {

	private static final Color HOVER   = new Color(54, 61, 77).lock();
	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color SHADOW  = new Color(0, 0, 0, 153).lock();

	private final StringSignal moved = new StringSignal("none");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(PlaylistUI.TEXT);

		RectNode
		.create(660, 240, 600, 600)
		.color(PlaylistUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PlaylistUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region list
			ReorderableFlexNode
			.vertical(60, 40, 480)
			// #region end
			.onReorderEnd(event -> {
				final String song = event.getChild().getLabel().getText();
				this.moved.set(song + " from " + event.getPrevious() + " to " + event.getIndex());
			})
			// #endregion
			.margin(10D)
			.<ReorderableFlexNode>body(list -> {
				for (final String song : new String[] {"Intro", "Ocean", "Night drive", "Echoes", "Outro"}) {
					RectNode
					.create(0, 0, 480, 70)
					.color(PlaylistUI.ELEMENT)
					.hoveredColor(PlaylistUI.HOVER)
					.label(Text.create(song, info))
					.body(row -> row.effect(ShadowNodeEffect.create(
						() -> list.isDragging(row) ? PlaylistUI.SHADOW : Color.TRANSPARENT,
						() -> 32F
					)))
					.attach(list);
				}
			})
			.attach(card);
			// #endregion
			TextNode.create(60, 480).text(Text.create("Moved: " + this.moved.get(), info)).attach(card);
		})
		.attach(this);
	}

}