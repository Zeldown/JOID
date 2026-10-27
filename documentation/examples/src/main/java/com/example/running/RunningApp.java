package com.example.running;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import com.example.ExampleFont;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.base.glfw.GlfwWindowBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;

public final class RunningApp {

	private RunningApp() {}

	public static void main(final String[] args) {
		// #region window
		GLFW.glfwInit();
		GlContextRequest.CORE_33.apply();
		final long window = GLFW.glfwCreateWindow(1280, 720, "My application", 0L, 0L);
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		// #endregion

		// #region register
		final Lwjgl3Backend backend = Lwjgl3Backend.create(window).register();
		// #endregion

		// #region bridge
		BridgeHandler.UI
		.getBridge(WindowUIBridge.class)
		.background(Color.decode("#DDDDDD"))
		.hoverInfo(TextInfo.create(ExampleFont.REGULAR, 20F).color(Color.WHITE));
		// #endregion

		// #region loop
		JOID.open(new RunningStartUI());
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			backend.frame();
			GLFW.glfwSwapBuffers(window);
		}

		BridgeHandler.UI.getBridge(WindowUIBridge.class).closeAll();
		BridgeHandler.WINDOW.getBridge(GlfwWindowBridge.class).destroy();
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		// #endregion
	}

}