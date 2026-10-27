package com.example.reference.backends;

import dev.joid.lib.bridge.ui.StackUIBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;

// #region bridge
public final class ScaledOverlayUIBridge extends StackUIBridge {

	private double scale;

	public ScaledOverlayUIBridge() {
		this.scale = 1D;
	}

	public ScaledOverlayUIBridge scale(final double scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public boolean canHandle(final Class<? extends UI> clazz) {
		final UIDataOverlay overlay = clazz.getAnnotation(UIDataOverlay.class);
		return overlay != null && overlay.active();
	}

	@Override
	public double getInterfaceScale(final UI ui) {
		return this.scale;
	}

}
// #endregion