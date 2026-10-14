package dev.joid.base.glfw.snapshot;

import org.lwjgl.glfw.GLFW;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwSnapshotWindow {

	private final long window;

	public static @NonNull GlfwSnapshotWindow create(final int width, final int height, final @NonNull Runnable hints) {
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("GLFW cannot be initialized, check that the system has a display (DISPLAY or WAYLAND_DISPLAY on Linux)");
		}

		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		hints.run();

		final long window = GLFW.glfwCreateWindow(width, height, "JOID snapshot", 0L, 0L);
		if (window == 0L) {
			throw new IllegalStateException("GLFW cannot create the " + width + "x" + height + " snapshot window, check that the graphics driver supports the OpenGL version the window asks for");
		}
		return new GlfwSnapshotWindow(window);
	}

	public void destroy() {
		GLFW.glfwDestroyWindow(this.window);
	}

}