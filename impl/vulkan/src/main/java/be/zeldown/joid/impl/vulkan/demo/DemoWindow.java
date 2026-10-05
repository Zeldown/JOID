package be.zeldown.joid.impl.vulkan.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import be.zeldown.joid.impl.vulkan.Backend;
import be.zeldown.joid.impl.vulkan.render.RenderBridge;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;

public class DemoWindow extends be.zeldown.joid.impl.glfw.DemoWindow {

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

	@Override
	protected String getEngineName() {
		return "Vulkan";
	}

	@Override
	protected void configureWindow() {
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
	}

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	@Override
	protected void registerBackend(final long window) {
		Backend.register(window);
	}

}