package be.zeldown.joid.test.snapshot;

import java.util.ArrayList;

import be.zeldown.joid.demo.DemoUIBridge;
import be.zeldown.joid.lib.ui.core.UI;

public final class SnapshotUIBridge extends DemoUIBridge {

	public void closeAll() {
		for (final UI ui : new ArrayList<>(super.getUiList().ordered())) {
			ui.properlyClose();
			super.getUiList().remove(ui);
		}
	}

}