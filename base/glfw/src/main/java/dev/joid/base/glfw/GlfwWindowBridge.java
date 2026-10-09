package dev.joid.base.glfw;

import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import dev.joid.base.glfw.input.GlfwKeys;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import lombok.Getter;
import lombok.NonNull;

public final class GlfwWindowBridge implements IWindowBridge {

	private final long              window;
	private final Map<Cursor, Long> cursorMap;

	@Getter
	private Cursor cursor;

	public GlfwWindowBridge(final long window) {
		this.window    = window;
		this.cursorMap = new EnumMap<>(Cursor.class);
		this.cursor    = Cursor.DEFAULT;
	}

	public void destroy() {
		for (final Long handle : this.cursorMap.values()) {
			if (handle != 0L) {
				GLFW.glfwDestroyCursor(handle);
			}
		}
		this.cursorMap.clear();
		this.cursor = Cursor.DEFAULT;
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
	public void setCursor(final @NonNull Cursor cursor) {
		GLFW.glfwSetCursor(this.window, cursor == Cursor.DEFAULT ? 0L : this.cursorMap.computeIfAbsent(cursor, GlfwWindowBridge::createCursor));
		this.cursor = cursor;
	}

	@Override
	public void setClipboard(final @NonNull String text) {
		GLFW.glfwSetClipboardString(this.window, text);
	}

	private boolean isCodeDown(final int code) {
		return GLFW.glfwGetKey(this.window, code) == GLFW.GLFW_PRESS;
	}

	private static long createCursor(final Cursor cursor) {
		switch (cursor) {
		case POINTER:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR);
		case TEXT:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
		case CROSSHAIR:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
		case MOVE:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR);
		case NOT_ALLOWED:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_NOT_ALLOWED_CURSOR);
		case RESIZE_EW:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_EW_CURSOR);
		case RESIZE_NS:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NS_CURSOR);
		case RESIZE_NWSE:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR);
		case RESIZE_NESW:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR);
		default:
			return GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
		}
	}

}