package dev.joid.impl.vulkan;

import dev.joid.impl.glfw.WindowBridge;
import dev.joid.impl.openal.AudioBridge;
import dev.joid.impl.vulkan.render.RenderBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register(final long window) {
		JOID.checkVersion(JOID.VERSION);
		BridgeHandler.AUDIO.register(new AudioBridge());
		BridgeHandler.WINDOW.register(new WindowBridge(window));
		BridgeHandler.RENDER.register(new RenderBridge(window));
	}

}