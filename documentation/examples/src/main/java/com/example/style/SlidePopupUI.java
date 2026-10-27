package com.example.style;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true)
@UIDataPopup(active = true)
public class SlidePopupUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	// #region set
	public SlidePopupUI() {
		super.transition(SlideTransition.create());
	}
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(SlidePopupUI.INK);

		RectNode.create(710, 390, 500, 300).color(Color.WHITE).label(Text.create("Saved", info)).attach(this);
	}

}