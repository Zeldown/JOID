package com.example.reference.backends;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;

public final class BackendLoop {

	private BackendLoop() {}

	public static void main(final String[] args) {
		GLFW.glfwInit();
		GlContextRequest.CORE_33.apply();
		final long window = GLFW.glfwCreateWindow(1280, 720, "Backend", 0L, 0L);
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();

		// #region loop
		final Lwjgl3Backend backend = Lwjgl3Backend
		.create(window)
		.audio(false)
		.register();

		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			backend.frame();
			GLFW.glfwSwapBuffers(window);
		}

		BridgeHandler.UI.getBridge(WindowUIBridge.class).closeAll();
		// #endregion

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}