package be.zeldown.joid.impl.lwjgl2;

import be.zeldown.joid.impl.lwjgl2.audio.AudioBridge;
import be.zeldown.joid.impl.lwjgl2.render.RenderBridge;
import be.zeldown.joid.impl.lwjgl2.window.WindowBridge;
import be.zeldown.joid.lib.bridge.BridgeHandler;

public final class Backend {

	public static void register() {
		BridgeHandler.AUDIO.register(new AudioBridge());
		BridgeHandler.WINDOW.register(new WindowBridge());
		BridgeHandler.RENDER.register(new RenderBridge());
	}

}