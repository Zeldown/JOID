package dev.joid.showcase;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.demo.DemoWindow;
import dev.joid.internal.JOID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowcaseWindow extends DemoWindow {

	public static @NonNull ShowcaseWindow create() {
		return new ShowcaseWindow();
	}

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		ShowcaseWindow.create().run();
	}

	@Override
	protected void openMenu() {
		GLFW.glfwSetWindowTitle(super.getWindow(), "JOID - Showcase (Vulkan)");
		GLFW.glfwMaximizeWindow(super.getWindow());
		JOID.open(new ShowMenu());
	}

}