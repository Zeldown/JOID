package dev.joid.lib.bridge.ui;

import dev.joid.lib.bridge.BridgeRegistry;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public final class UIBridgeRegistry extends BridgeRegistry<IUIBridge> {

	private UIBridgeRegistry() {
		super("UI");
	}

	public static @NonNull UIBridgeRegistry create() {
		return new UIBridgeRegistry();
	}

	public IUIBridge get(final @NonNull UI ui) {
		final IUIBridge holder = super.find(bridge -> bridge.isOpen(ui));
		return holder != null ? holder : super.find(bridge -> bridge.canHandle(ui));
	}

	public IUIBridge get(final @NonNull Class<? extends UI> clazz) {
		return super.find(bridge -> bridge.canHandle(clazz));
	}

}