package be.zeldown.joid.lib.ui.bridge;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.list.IndexedLinkedList;
import lombok.NonNull;

public final class BridgeHandler {

	private static final IndexedLinkedList<IUIBridge> BRIDGE_LIST = new IndexedLinkedList<>();

	public static void register(final @NonNull IUIBridge bridge) {
		BridgeHandler.BRIDGE_LIST.add(bridge);
	}

	public static IUIBridge get(final @NonNull UI ui) {
		return BridgeHandler.get(ui.getClass());
	}

	public static IUIBridge get(final @NonNull Class<? extends UI> clazz) {
		for (final IUIBridge bridge : BridgeHandler.BRIDGE_LIST.reversed()) {
			if (bridge.canHandle(clazz)) {
				return bridge;
			}
		}

		return null;
	}

}