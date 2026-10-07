package dev.joid.impl.vulkan.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.impl.vulkan.Backend;
import dev.joid.impl.vulkan.render.RenderBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

public class DemoWindow extends dev.joid.impl.glfw.demo.DemoWindow {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	@Override
	protected String getEngineName() {
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
	protected void endFrame() {
		final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
		render.endFrame();
		render.present();
	}

	@Override
	protected void beginFrame() {
		((RenderBridge) BridgeHandler.RENDER.get()).beginFrame();
	}

}