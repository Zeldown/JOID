package dev.joid.impl.lwjgl3;

import dev.joid.impl.glfw.WindowBridge;
import dev.joid.impl.lwjgl3.render.RenderBridge;
import dev.joid.impl.openal.AudioBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register(final long window) {
		JOID.checkVersion(JOID.VERSION);
		BridgeHandler.AUDIO.register(new AudioBridge());
		BridgeHandler.RENDER.register(new RenderBridge());
		BridgeHandler.WINDOW.register(new WindowBridge(window));
	}

}