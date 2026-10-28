package com.example.running;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.VulkanBackend;
import dev.joid.backend.vulkan.host.WindowVulkanHost;
import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;

public final class VulkanApp {

	private VulkanApp() {}

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		GLFW.glfwInit();
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		final long window = GLFW.glfwCreateWindow(1280, 720, "My application", 0L, 0L);

		final WindowVulkanHost host = WindowVulkanHost.create(window);
		final VulkanBackend backend = VulkanBackend.create(window, host).register();
		host.beginFrame();

		JOID.open(new RunningStartUI());
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			backend.frame();
			host.endFrame();
			if (!GLFW.glfwWindowShouldClose(window)) {
				host.beginFrame();
			}
		}

		BridgeHandler.UI.getBridge(WindowUIBridge.class).closeAll();
		backend.destroy();
		host.destroy();
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}