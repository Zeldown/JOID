package com.example.state;

import java.util.Arrays;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class RebuildUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	// #region signal
	private final ListSignal<String> items = new ListSignal<>(Arrays.asList("First item", "Second item"));
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(RebuildUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		// #region buttons
		RectNode
		.create(660, 240, 280, 60)
		.color(RebuildUI.INK)
		.onClick(event -> this.items.add("Item " + (this.items.size() + 1)))
		.label(Text.create("Add", label))
		.attach(this);

		RectNode
		.create(980, 240, 280, 60)
		.color(RebuildUI.INK)
		.onClick(event -> this.items.remove(this.items.size() - 1))
		.visible(!this.items.isEmpty())
		.label(Text.create("Remove", label))
		.attach(this);
		// #endregion

		// #region watch
		FlexNode
		.vertical(660, 340, 600)
		.margin(10D)
		.watch(this.items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
		.body(flex -> {
			for (final String item : this.items.peek()) {
				RectNode
				.create(0, 0, 600, 60)
				.color(Color.WHITE)
				.body(row -> {
					TextNode.create(20, 30).text(Text.create(item, info)).anchorY(Align.CENTER).attach(row);
				})
				.attach(flex);
			}
		})
		.attach(this);
		// #endregion
	}

}