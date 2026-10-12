package dev.joid.test.snapshot;

import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public final class SnapshotUIBridge extends WindowUIBridge {

	private static final Color BACKGROUND = new Color(50, 50, 50);

	private double interfaceScale = 1D;

	public SnapshotUIBridge() {
		super.background(SnapshotUIBridge.BACKGROUND);
	}

	@Override
	public double getInterfaceScale(final @NonNull UI ui) {
		return this.interfaceScale;
	}

	@Override
	public TextInfo getHoverInfo() {
		return DevFont.MONTSERRAT != null ? TextInfo.create(DevFont.MONTSERRAT, 20, Color.WHITE) : null;
	}

	public @NonNull SnapshotUIBridge interfaceScale(final double interfaceScale) {
		this.interfaceScale = interfaceScale;
		return this;
	}

}