package dev.joid.test.snapshot;

import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public final class SnapshotUIBridge extends DemoUIBridge {

	private double interfaceScale = 1D;

	@Override
	public double getInterfaceScale(final @NonNull UI ui) {
		return this.interfaceScale;
	}

	public @NonNull SnapshotUIBridge interfaceScale(final double interfaceScale) {
		this.interfaceScale = interfaceScale;
		return this;
	}

}