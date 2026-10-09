package dev.joid.backend.lwjgl3;

import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.glfw.WindowBridge;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.openal.binding.Lwjgl3AlBinding;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.resource.GlTextureResourceResolver;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.resource.dto.resolver.ResourceResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register(final long window) {
		Backend.register(window, Lwjgl3GlBinding.inst());
	}

	public static void register(final long window, final @NonNull IGlBinding binding) {
		JOID.checkVersion(JOID.VERSION);
		BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl3AlBinding.inst()));
		BridgeHandler.RENDER.register(GlRenderBridge.create(binding));
		BridgeHandler.WINDOW.register(new WindowBridge(window));
		ResourceResolver.register(GlTextureResourceResolver.inst());
	}

}