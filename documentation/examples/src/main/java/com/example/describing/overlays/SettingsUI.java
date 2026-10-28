package com.example.describing.overlays;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class SettingsUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color MUTED     = new Color(139, 147, 163).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 48F).color(SettingsUI.TEXT);
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(SettingsUI.MUTED);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 26F).color(SettingsUI.ON_ACCENT);

		TextNode.create(480, 250).text(Text.create("Settings", title)).attach(this);

		TextNode.create(480, 340).text(Text.create("Music: on", info)).attach(this);

		TextNode.create(480, 390).text(Text.create("Volume: 80 %", info)).attach(this);

		// #region open
		RectNode
		.create(860, 500, 200, 80)
		.color(SettingsUI.ACCENT)
		.label(Text.create("Quit", label))
		.onClick(event -> JOID.open(new ConfirmPopup()))
		.attach(this);
		// #endregion
	}

}