package dev.joid.backend.vulkan.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.Backend;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.base.glfw.demo.GlfwDemoWindow;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

public class DemoWindow extends GlfwDemoWindow {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	@Override
	protected String getBackendName() {
		return "Vulkan";
	}

	@Override
	protected void configureWindow() {
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
	}

	@Override
	protected void registerBackend(final long window) {
		Backend.register(window);
	}

	@Override
	protected void present() {
		((VulkanRenderBridge) BridgeHandler.RENDER.get()).present();
	}

}