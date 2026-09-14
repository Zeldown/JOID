package be.zeldown.joid.impl.vulkan.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import be.zeldown.joid.impl.glfw.GLFWDemoWindow;
import be.zeldown.joid.impl.vulkan.VulkanBackend;
import be.zeldown.joid.impl.vulkan.render.VulkanRenderBridge;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;

public class DemoWindow extends GLFWDemoWindow {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	@Override
	protected void configureWindow() {
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
	}

	@Override
	protected void registerBackend(final long window) {
		VulkanBackend.register(window);
	}

	@Override
	protected void beginFrame() {
		((VulkanRenderBridge) BridgeHandler.RENDER.get()).beginFrame();
	}

	@Override
	protected void endFrame() {
		final VulkanRenderBridge render = (VulkanRenderBridge) BridgeHandler.RENDER.get();
		render.endFrame();
		render.present();
	}

}