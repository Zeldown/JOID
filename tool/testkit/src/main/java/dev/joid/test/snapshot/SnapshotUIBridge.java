package dev.joid.test.snapshot;

import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

public final class SnapshotUIBridge extends WindowUIBridge {

	private static final Color BACKGROUND = new Color(50, 50, 50).lock();

	@Setter
	@Accessors(fluent = true, chain = true)
	private double interfaceScale = 1D;

	private SnapshotUIBridge() {
		super.background(SnapshotUIBridge.BACKGROUND);
	}

	public static @NonNull SnapshotUIBridge create() {
		return new SnapshotUIBridge();
	}

	@Override
	public double getInterfaceScale(final @NonNull UI ui) {
		return this.interfaceScale;
	}

}