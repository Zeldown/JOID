package dev.joid.backend.vulkan;

import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.backend.vulkan.resource.VulkanImageResourceResolver;
import dev.joid.base.glfw.GlfwWindowBridge;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.openal.binding.Lwjgl3AlBinding;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.resource.resolver.ResourceResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Backend {

	public static void register(final long window) {
		JOID.checkVersion(JOID.VERSION);
		BridgeHandler.AUDIO.register(AlAudioBridge.create(Lwjgl3AlBinding.inst()));
		BridgeHandler.WINDOW.register(new GlfwWindowBridge(window));
		BridgeHandler.RENDER.register(new VulkanRenderBridge(window));
		ResourceResolver.register(VulkanImageResourceResolver.inst());
	}

}