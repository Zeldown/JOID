package dev.joid.showcase;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.demo.DemoWindow;
import dev.joid.internal.JOID;

public class ShowcaseWindow extends DemoWindow {

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		new ShowcaseWindow().run();
	}

	@Override
	protected void openMenu() {
		GLFW.glfwSetWindowTitle(super.getWindow(), "JOID - Showcase (Vulkan)");
		GLFW.glfwMaximizeWindow(super.getWindow());
		JOID.open(new ShowMenu());
	}

}