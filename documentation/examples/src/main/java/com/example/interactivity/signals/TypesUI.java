package com.example.interactivity.signals;

import java.util.Arrays;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class TypesUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	// #region types
	private final BooleanSignal      details = new BooleanSignal(true);
	private final ListSignal<String> names   = new ListSignal<>(Arrays.asList("Ada", "Grace"));
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(TypesUI.INK);

		RectNode
		.create(560, 340, 800, 400)
		.color(TypesUI.PLACEHOLDER)
		.body(card -> {
			// #region use
			RectNode
			.create(60, 40, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> this.names.add("Linus"))
			.label(Text.create("Add Linus", info))
			.attach(card);
			RectNode
			.create(300, 40, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> this.names.remove("Ada"))
			.label(Text.create("Remove Ada", info))
			.attach(card);
			RectNode
			.create(540, 40, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> this.details.toggle())
			.label(Text.create("Details", info))
			.attach(card);
			TextNode.create(60, 150).text(Text.create("Count: " + this.names.size(), info)).attach(card);
			TextNode
			.create(60, 200)
			.text(Text.create("First: " + this.names.get(0), info))
			.visible(this.details.get())
			.attach(card);
			TextNode
			.create(60, 250)
			.text(Text.create("Has Linus: " + this.names.contains("Linus"), info))
			.visible(this.details.get())
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}