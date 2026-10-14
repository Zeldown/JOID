package dev.joid.demo.ui;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.UI;

public class UIDemo extends UI {

	public UIDemo() {
		super.beforeClose(event -> {
			if (!super.getData().active()) {
				return;
			}

			super.getData().active(false);
			JOID.open(new UIDemoChoice());
			event.cancel();
		});
	}

}