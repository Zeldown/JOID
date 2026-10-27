package dev.joid.demo.ui;

import dev.joid.lib.JOID;
import dev.joid.lib.ui.core.UI;

public class UIDemo extends UI {

	public UIDemo() {
		super.beforeClose(event -> {
			if (!super.getData().active()) {
				return;
			}

			super.getData().active(false);
			JOID.open(UIDemoChoice.getMenu().get());
			event.cancel();
		});
	}

}