package com.example.running;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import com.example.ExampleFont;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;

public final class HostApp {

	private static final Color TEXT = new Color(231, 233, 238).lock();

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
		bridge.hoverInfo(TextInfo.create(ExampleFont.REGULAR, 20F).color(HostApp.TEXT));
		// #endregion

		// #region loop
		final HostScene scene = new HostScene();
		JOID.open(new HostOverlayUI());
		double last = GLFW.glfwGetTime();
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			final double now = GLFW.glfwGetTime();
			scene.update(now - last);
			last = now;
			scene.draw(window);
			backend.frame();
			GLFW.glfwSwapBuffers(window);
		}

		bridge.closeAll();
		// #endregion
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}