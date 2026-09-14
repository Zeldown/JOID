package be.zeldown.joid.lib.bridge;

import be.zeldown.joid.lib.bridge.audio.IAudioBridge;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.ui.UIBridgeRegistry;
import be.zeldown.joid.lib.bridge.window.IWindowBridge;

public final class BridgeHandler {

	public static final UIBridgeRegistry              UI;
	public static final BridgeRegistry<IWindowBridge> WINDOW;
	public static final BridgeRegistry<IRenderBridge> RENDER;
	public static final BridgeRegistry<IAudioBridge>  AUDIO;

	static {
		UI     = UIBridgeRegistry.create();
		WINDOW = BridgeRegistry.create("WINDOW");
		RENDER = BridgeRegistry.create("RENDER");
		AUDIO  = BridgeRegistry.create("AUDIO");
	}

}