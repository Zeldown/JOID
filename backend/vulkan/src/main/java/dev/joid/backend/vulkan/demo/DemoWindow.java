package dev.joid.backend.vulkan.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.VulkanBackend;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.base.glfw.demo.GlfwDemoWindow;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.backend.Backend;
import lombok.NonNull;

public class DemoWindow extends GlfwDemoWindow {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		new DemoWindow().run();
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
	protected @NonNull Backend<?> registerBackend(final long window) {
		return VulkanBackend.create(window).register();
	}

	@Override
	protected void present() {
		((VulkanRenderBridge) BridgeHandler.RENDER.get()).present();
	}

}