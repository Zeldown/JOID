package dev.joid.backend.lwjgl2;

import dev.joid.backend.lwjgl2.binding.Lwjgl2AlBinding;
import dev.joid.backend.lwjgl2.render.RenderBridge;
import dev.joid.backend.lwjgl2.window.WindowBridge;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register() {
		JOID.checkVersion(JOID.VERSION);
		Natives.install();
		BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl2AlBinding.inst()));
		BridgeHandler.WINDOW.register(new WindowBridge());
		BridgeHandler.RENDER.register(new RenderBridge());
	}

}