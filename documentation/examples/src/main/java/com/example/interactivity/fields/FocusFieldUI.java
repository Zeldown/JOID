package com.example.interactivity.fields;

import com.example.ExampleFont;
import com.example.kit.KitTextFieldNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class FocusFieldUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final BooleanSignal focused = new BooleanSignal(false);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(FocusFieldUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(FocusFieldUI.PLACEHOLDER)
		.body(card -> {
			// #region focus
			final KitTextFieldNode search = KitTextFieldNode
			.create(60, 60, 480, 60)
			.info(info)
			.placeholder("Search")
			.focused(this.focused)
			.beforeBlur(event -> {
				if (event.getNode().getText().isEmpty()) {
					event.cancel();
				}
			})
			.attach(card);
			RectNode
			.create(60, 170, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> search.focus())
			.label(Text.create("Search", info))
			.attach(card);
			// #endregion
			TextNode.create(300, 185).text(Text.create(this.focused.get() ? "Focused" : "Not focused", info)).attach(card);
		})
		.attach(this);
	}

}