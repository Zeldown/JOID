package dev.joid.backend.lwjgl3;

import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.glfw.WindowBridge;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.openal.binding.Lwjgl3AlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register(final long window) {
		JOID.checkVersion(JOID.VERSION);
		BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl3AlBinding.inst()));
		BridgeHandler.RENDER.register(GlRenderBridge.create(Lwjgl3GlBinding.inst()));
		BridgeHandler.WINDOW.register(new WindowBridge(window));
	}

}