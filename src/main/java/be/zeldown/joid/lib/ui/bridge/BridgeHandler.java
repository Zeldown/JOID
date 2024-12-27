package be.zeldown.joid.lib.ui.bridge;

import java.util.ArrayList;
import java.util.List;

import be.zeldown.joid.lib.ui.core.UI;
import lombok.NonNull;

public final class BridgeHandler {

	private static final List<IUIBridge> BRIDGE_LIST = new ArrayList<>();

	public static void register(final @NonNull IUIBridge bridge) {
		BridgeHandler.BRIDGE_LIST.add(bridge);
	}

	public static IUIBridge get(final @NonNull UI ui) {
		return BridgeHandler.get(ui.getClass());
	}

	public static IUIBridge get(final @NonNull Class<? extends UI> clazz) {
		for (final IUIBridge bridge : BridgeHandler.BRIDGE_LIST) {
			if (bridge.canHandle(clazz)) {
				return bridge;
			}
		}

		return null;
	}

}