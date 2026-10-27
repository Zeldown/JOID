package com.example.start;

import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region popup
@UIData(background = true)
@UIDataPopup(active = true)
public class NoticeUI extends UI {

	@Override
	public void init() {
		RectNode
		.create(760, 390, 400, 300)
		.color(Color.WHITE)
		.onClick(event -> JOID.close(this))
		.attach(this);
	}

}
// #endregion