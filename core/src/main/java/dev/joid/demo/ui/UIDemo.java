package dev.joid.demo.ui;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.UI;

public class UIDemo extends UI {

	@Override
	public boolean close() {
		if (!super.getData().active()) {
			return true;
		}

		super.getData().setActive(false);
		JOID.open(new UIDemoChoice());
		return false;
	}

}