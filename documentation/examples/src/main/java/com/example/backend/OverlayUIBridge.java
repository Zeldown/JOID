package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.lib.bridge.ui.StackUIBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;

// #region bridge
public class OverlayUIBridge extends StackUIBridge {

	private final Engine engine;

	protected OverlayUIBridge(final Engine engine) {
		this.engine = engine;
	}

	public static OverlayUIBridge create(final Engine engine) {
		return new OverlayUIBridge(engine);
	}

	@Override
	public boolean canHandle(final Class<? extends UI> clazz) {
		final UIDataOverlay overlay = clazz.getAnnotation(UIDataOverlay.class);
		return overlay != null && overlay.active();
	}

	@Override
	public double getInterfaceScale(final UI ui) {
		return 1.25D;
	}

	@Override
	public boolean isOverlayHidden() {
		return this.engine.isInterfaceHidden();
	}

}
// #endregion