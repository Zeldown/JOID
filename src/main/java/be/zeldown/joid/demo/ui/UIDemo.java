package be.zeldown.joid.demo.ui;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.ui.core.UI;

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