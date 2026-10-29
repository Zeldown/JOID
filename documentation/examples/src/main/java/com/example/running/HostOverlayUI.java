package com.example.running;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.core.data.UIDataOverlayInteraction;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.utils.align.Align;

@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true))
public class HostOverlayUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(HostOverlayUI.TEXT);

		Shortcut.create(Key.TAB).onTrigger(event -> JOID.open(new HostMenuUI())).attach(this);

		RectNode
		.create(40, 40, 220, 60)
		.color(HostOverlayUI.ELEMENT)
		.body(rect -> {
			TextNode
			.create(0, 0, 220, 60)
			.text(Text.create("Tab: menu", info).align(Align.CENTER))
			.attach(rect);
		})
		.attach(this);
	}

}