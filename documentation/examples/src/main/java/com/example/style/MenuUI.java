package com.example.style;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class MenuUI extends UI {

	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(MenuUI.ON_ACCENT);

		RectNode.create(560, 160, 240, 60).color(MenuUI.ACCENT).onClick(event -> JOID.open(new PopPopupUI())).label(Text.create("Pop", label)).attach(this);

		RectNode.create(840, 160, 240, 60).color(MenuUI.ACCENT).onClick(event -> JOID.open(new SlidePopupUI())).label(Text.create("Slide", label)).attach(this);

		// #region open
		RectNode
		.create(1120, 160, 240, 60)
		.color(MenuUI.ACCENT)
		.onClick(event -> JOID.open(new PopPopupUI().transition(SlideTransition.create())))
		.label(Text.create("Pop, sliding", label))
		.attach(this);
		// #endregion
	}

}