package dev.joid.showcase;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.demo.DemoWindow;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

public class ShowcaseWindow extends DemoWindow {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		final ShowcaseWindow window = new ShowcaseWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	@Override
	public void start() {
		GLFW.glfwSetWindowTitle(super.getWindow(), "JOID - Showcase (Vulkan)");
		GLFW.glfwMaximizeWindow(super.getWindow());
		JOID.open(new ShowMenu());
		super.load();
	}

}