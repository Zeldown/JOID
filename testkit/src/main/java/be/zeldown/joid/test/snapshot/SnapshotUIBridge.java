package be.zeldown.joid.test.snapshot;

import java.util.ArrayList;

import be.zeldown.joid.demo.DemoUIBridge;
import be.zeldown.joid.lib.ui.core.UI;
import lombok.NonNull;

public final class SnapshotUIBridge extends DemoUIBridge {

	private double interfaceScale = 1D;

	public void closeAll() {
		for (final UI ui : new ArrayList<>(super.getUiList().ordered())) {
			ui.properlyClose();
			super.getUiList().remove(ui);
		}
	}

	@Override
	public double getInterfaceScale(final @NonNull UI ui) {
		return this.interfaceScale;
	}

	public @NonNull SnapshotUIBridge interfaceScale(final double interfaceScale) {
		this.interfaceScale = interfaceScale;
		return this;
	}

}