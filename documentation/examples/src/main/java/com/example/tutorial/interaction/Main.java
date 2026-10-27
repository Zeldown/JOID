package com.example.tutorial.interaction;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;

public final class Main {

	private Main() {}

	public static void main(final String[] arguments) {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GlContextRequest.CORE_33.apply();
		final long window = GLFW.glfwCreateWindow(1280, 720, "Starfall", 0L, 0L);
		if (window == 0L) {
			throw new IllegalStateException("Unable to create the window");
		}

		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		final Lwjgl3Backend backend = Lwjgl3Backend.create(window).register();
		// #region tooltips
		final WindowUIBridge bridge = BridgeHandler.UI.getBridge(WindowUIBridge.class);
		bridge.hoverInfo(TextInfo.create(Theme.FONT, 24F).color(Color.WHITE));
		// #endregion

		// #region loop
		JOID.open(new MenuUI());
		while (!GLFW.glfwWindowShouldClose(window) && JOID.isOpen(MenuUI.class)) {
			GLFW.glfwPollEvents();
			backend.frame();
			GLFW.glfwSwapBuffers(window);
		}
		// #endregion

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}