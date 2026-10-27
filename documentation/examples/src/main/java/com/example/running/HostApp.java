package com.example.running;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.example.ExampleFont;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;

public final class HostApp {

	private static boolean paused;

	private HostApp() {}

	public static void main(final String[] args) {
		GLFW.glfwInit();
		GlContextRequest.CORE_33.apply();
		final long window = GLFW.glfwCreateWindow(1280, 720, "My game", 0L, 0L);
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();

		// #region callbacks
		GLFW.glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
				GLFW.glfwSetWindowShouldClose(handle, true);
			}
		});
		// #endregion

		// #region scene
		final SceneUIBridge bridge = SceneUIBridge.create();
		BridgeHandler.UI.register(bridge);
		final Lwjgl3Backend backend = Lwjgl3Backend.create(window).register();
		bridge.hoverInfo(TextInfo.create(ExampleFont.REGULAR, 20F).color(Color.WHITE));
		// #endregion

		// #region loop
		JOID.open(new HostHudUI());
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			GL11.glClearColor(0.1F, 0.15F, 0.3F, 1F);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			backend.frame();
			GLFW.glfwSwapBuffers(window);
		}

		bridge.closeAll();
		// #endregion
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

	public static void togglePause() {
		HostApp.paused = !HostApp.paused;
	}

	public static boolean isPaused() {
		return HostApp.paused;
	}

}