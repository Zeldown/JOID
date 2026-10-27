package com.example.describing.overlays;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class SettingsUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 26F).color(Color.WHITE);

		RectNode.create(0, 0, 1920, 1080).color(SettingsUI.PLACEHOLDER).attach(this);

		// #region open
		RectNode
		.create(860, 500, 200, 80)
		.color(SettingsUI.INK)
		.label(Text.create("Quit", label))
		.onClick(event -> JOID.open(new ConfirmPopup()))
		.attach(this);
		// #endregion
	}

}