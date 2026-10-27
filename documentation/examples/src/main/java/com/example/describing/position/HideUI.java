package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class HideUI extends UI {

	private static final Color INK  = new Color(153, 153, 153).lock();
	private static final Color DARK = new Color(85, 85, 85).lock();

	private final BooleanSignal shown = new BooleanSignal(true);

	@Override
	public void init() {
		// #region hide
		RectNode.create(660, 440, 180, 180).color(HideUI.INK).visible(this.shown).attach(this);

		RectNode.create(870, 440, 180, 180).color(HideUI.INK).hoveredColor(HideUI.DARK).enabled(false).attach(this);

		RectNode.create(1080, 440, 180, 180).color(Color.WHITE).onClick(event -> this.shown.toggle()).attach(this);
		// #endregion
	}

}