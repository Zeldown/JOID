package com.example.further;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class KitUI extends UI {

	@Override
	public void init() {
		// #region use
		KitCheckboxNode.create(100, 100, 40).checked(true).attach(this);

		KitIntegerSliderNode.create(100, 180, 300, 40).range(0, 10).value(4).attach(this);

		KitSwitchNode.<String>create(100, 260, 300, 50).values("Low", "Medium", "High").value("Medium").attach(this);

		KitPillToggleNode.create(100, 350, 80, 40).attach(this);
		// #endregion
	}

}