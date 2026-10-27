package com.example.running;

import org.lwjgl.glfw.GLFW;

import dev.joid.lib.bridge.ui.WindowUIBridge;

public class GameUIBridge extends WindowUIBridge {

	private final long window;

	private boolean paused;

	protected GameUIBridge(final long window) {
		this.window = window;
	}

	public static GameUIBridge create(final long window) {
		return new GameUIBridge(window);
	}

	public boolean isPaused() {
		return this.paused;
	}

	@Override
	protected void attachScreen() {
		this.paused = true;
		GLFW.glfwSetInputMode(this.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	}

	@Override
	protected void detachScreen() {
		this.paused = false;
		GLFW.glfwSetInputMode(this.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
	}

}