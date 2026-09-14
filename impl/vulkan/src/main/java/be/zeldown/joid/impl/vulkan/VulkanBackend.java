package be.zeldown.joid.impl.vulkan;

import be.zeldown.joid.impl.glfw.GLFWWindowBridge;
import be.zeldown.joid.impl.openal.OpenALAudioBridge;
import be.zeldown.joid.impl.vulkan.render.VulkanRenderBridge;
import be.zeldown.joid.lib.bridge.BridgeHandler;

public final class VulkanBackend {

	public static void register(final long window) {
		BridgeHandler.AUDIO.register(new OpenALAudioBridge());
		BridgeHandler.WINDOW.register(new GLFWWindowBridge(window));
		BridgeHandler.RENDER.register(new VulkanRenderBridge(window));
	}

}