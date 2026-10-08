package dev.joid.base.glfw;

import org.lwjgl.glfw.GLFW;

import dev.joid.base.glfw.input.GlfwKeys;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class WindowBridge implements IWindowBridge {

	private final long window;

	public WindowBridge(final long window) {
		this.window = window;
	}

	@Override
	public int getWidth() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		return width[0];
	}

	@Override
	public int getHeight() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		return height[0];
	}

	@Override
	public double getMouseX() {
		final double[] x = new double[1];
		final double[] y = new double[1];
		GLFW.glfwGetCursorPos(this.window, x, y);

		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetWindowSize(this.window, width, height);
		return GlfwWindows.toFramebuffer(x[0], width[0], this.getWidth());
	}

	@Override
	public double getMouseY() {
		final double[] x = new double[1];
		final double[] y = new double[1];
		GLFW.glfwGetCursorPos(this.window, x, y);

		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetWindowSize(this.window, width, height);
		return GlfwWindows.toFramebuffer(y[0], height[0], this.getHeight());
	}

	@Override
	public @NonNull String getClipboard() {
		final String clipboard = GLFW.glfwGetClipboardString(this.window);
		return clipboard == null ? "" : clipboard;
	}

	@Override
	public boolean isMouseGrabbed() {
		return GLFW.glfwGetInputMode(this.window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED;
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		return GlfwKeys.isKeyDown(key, this::isCodeDown);
	}

	@Override
	public boolean isPhysicalKeyDown(final @NonNull Key key) {
		return GlfwKeys.isPhysicalKeyDown(key, this::isCodeDown);
	}

	@Override
	public void setClipboard(final @NonNull String text) {
		GLFW.glfwSetClipboardString(this.window, text);
	}

	private boolean isCodeDown(final int code) {
		return GLFW.glfwGetKey(this.window, code) == GLFW.GLFW_PRESS;
	}

}