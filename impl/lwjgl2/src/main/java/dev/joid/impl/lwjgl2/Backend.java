package dev.joid.impl.lwjgl2;

import dev.joid.impl.lwjgl2.audio.AudioBridge;
import dev.joid.impl.lwjgl2.render.RenderBridge;
import dev.joid.impl.lwjgl2.window.WindowBridge;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register() {
		BridgeHandler.AUDIO.register(new AudioBridge());
		BridgeHandler.WINDOW.register(new WindowBridge());
		BridgeHandler.RENDER.register(new RenderBridge());
	}

}