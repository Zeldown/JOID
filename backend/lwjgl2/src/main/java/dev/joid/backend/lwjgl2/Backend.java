package dev.joid.backend.lwjgl2;

import dev.joid.backend.lwjgl2.binding.Lwjgl2AlBinding;
import dev.joid.backend.lwjgl2.binding.Lwjgl2GlBinding;
import dev.joid.backend.lwjgl2.window.WindowBridge;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.resource.GlTextureResourceResolver;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.resource.dto.resolver.ResourceResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register() {
		JOID.checkVersion(JOID.VERSION);
		Natives.install();
		BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl2AlBinding.inst()));
		BridgeHandler.WINDOW.register(new WindowBridge());
		BridgeHandler.RENDER.register(GlRenderBridge.create(Lwjgl2GlBinding.inst()));
		ResourceResolver.register(GlTextureResourceResolver.inst());
	}

}