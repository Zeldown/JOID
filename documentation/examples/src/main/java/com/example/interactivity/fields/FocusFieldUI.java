package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class FocusFieldUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private final BooleanSignal focused = new BooleanSignal(false);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(FocusFieldUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(FocusFieldUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FocusFieldUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region focus
			final TextFieldNode search = TextFieldNode
			.create(60, 60, 480, 60)
			.info(info)
			.placeholder("Search")
			.focused(this.focused)
			.beforeBlur(event -> {
				if (event.getNode().getText().isEmpty()) {
					event.cancel();
				}
			})
			.background(RectNode
			.create(0, 0, 0, 0)
			.color(FocusFieldUI.ELEMENT)
			.effect(BorderNodeEffect.create(FocusFieldUI.ACCENT, 2F).mode(BorderMode.IN).width(() -> this.focused.get() ? 2F : 0F)))
			.marginHorizontal(12D)
			.attach(card);
			RectNode
			.create(60, 170, 200, 60)
			.color(FocusFieldUI.ELEMENT)
			.onClick(event -> search.focus())
			.label(Text.create("Search", info))
			.attach(card);
			// #endregion
			TextNode.create(300, 185).text(Text.create(this.focused.get() ? "Focused" : "Not focused", info)).attach(card);
		})
		.attach(this);
	}

}