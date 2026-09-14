package be.zeldown.joid.impl.lwjgl2;

import be.zeldown.joid.impl.lwjgl2.audio.LWJGL2AudioBridge;
import be.zeldown.joid.impl.lwjgl2.render.LWJGL2RenderBridge;
import be.zeldown.joid.impl.lwjgl2.window.LWJGL2WindowBridge;
import be.zeldown.joid.lib.bridge.BridgeHandler;

public final class LWJGL2Backend {

	public static void register() {
		BridgeHandler.AUDIO.register(new LWJGL2AudioBridge());
		BridgeHandler.WINDOW.register(new LWJGL2WindowBridge());
		BridgeHandler.RENDER.register(new LWJGL2RenderBridge());
	}

}