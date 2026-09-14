package be.zeldown.joid.impl.lwjgl3;

import be.zeldown.joid.impl.glfw.GLFWWindowBridge;
import be.zeldown.joid.impl.lwjgl3.render.LWJGL3RenderBridge;
import be.zeldown.joid.impl.openal.OpenALAudioBridge;
import be.zeldown.joid.lib.bridge.BridgeHandler;

public final class LWJGL3Backend {

	public static void register(final long window) {
		BridgeHandler.AUDIO.register(new OpenALAudioBridge());
		BridgeHandler.RENDER.register(new LWJGL3RenderBridge());
		BridgeHandler.WINDOW.register(new GLFWWindowBridge(window));
	}

}