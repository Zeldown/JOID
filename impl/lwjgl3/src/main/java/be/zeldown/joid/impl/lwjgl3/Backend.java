package be.zeldown.joid.impl.lwjgl3;

import be.zeldown.joid.impl.glfw.WindowBridge;
import be.zeldown.joid.impl.lwjgl3.render.RenderBridge;
import be.zeldown.joid.impl.openal.AudioBridge;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register(final long window) {
		BridgeHandler.AUDIO.register(new AudioBridge());
		BridgeHandler.RENDER.register(new RenderBridge());
		BridgeHandler.WINDOW.register(new WindowBridge(window));
	}

}