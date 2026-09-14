package be.zeldown.joid.lib.bridge;

import be.zeldown.joid.lib.bridge.audio.IAudioBridge;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.ui.IUIBridge;
import be.zeldown.joid.lib.bridge.window.IWindowBridge;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.list.IndexedLinkedList;
import lombok.NonNull;

public final class BridgeHandler {

	private static final IndexedLinkedList<IUIBridge> BRIDGE_LIST = new IndexedLinkedList<>();

	private static IWindowBridge windowBridge;
	private static IRenderBridge renderBridge;
	private static IAudioBridge  audioBridge;

	public static void register(final @NonNull IUIBridge bridge) {
		BridgeHandler.BRIDGE_LIST.add(bridge);
	}

	public static void register(final @NonNull IWindowBridge bridge) {
		BridgeHandler.windowBridge = bridge;
	}

	public static void register(final @NonNull IRenderBridge bridge) {
		BridgeHandler.renderBridge = bridge;
	}

	public static void register(final @NonNull IAudioBridge bridge) {
		BridgeHandler.audioBridge = bridge;
	}

	public static IUIBridge get(final @NonNull UI ui) {
		for (final IUIBridge bridge : BridgeHandler.BRIDGE_LIST.reversed()) {
			if (bridge.canHandle(ui)) {
				return bridge;
			}
		}
		return null;
	}

	public static IUIBridge get(final @NonNull Class<? extends UI> clazz) {
		for (final IUIBridge bridge : BridgeHandler.BRIDGE_LIST.reversed()) {
			if (bridge.canHandle(clazz)) {
				return bridge;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends IUIBridge> T getBridge(final @NonNull Class<? extends T> bridgeClass) {
		for (final IUIBridge bridge : BridgeHandler.BRIDGE_LIST) {
			if (bridgeClass.isInstance(bridge)) {
				return (T) bridge;
			}
		}
		return null;
	}

	public static @NonNull IWindowBridge getWindow() {
		if (BridgeHandler.windowBridge == null) {
			throw new IllegalStateException("No window bridge registered, call BridgeHandler.register(IWindowBridge) before using JOID");
		}
		return BridgeHandler.windowBridge;
	}

	public static @NonNull IRenderBridge getRender() {
		if (BridgeHandler.renderBridge == null) {
			throw new IllegalStateException("No render bridge registered, call BridgeHandler.register(IRenderBridge) before using JOID");
		}
		return BridgeHandler.renderBridge;
	}

	public static @NonNull IAudioBridge getAudio() {
		if (BridgeHandler.audioBridge == null) {
			throw new IllegalStateException("No audio bridge registered, call BridgeHandler.register(IAudioBridge) before playing audio");
		}
		return BridgeHandler.audioBridge;
	}

}