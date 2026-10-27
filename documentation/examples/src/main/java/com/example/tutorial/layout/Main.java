package com.example.tutorial.layout;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.internal.JOID;

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

		JOID.open(new MenuUI());
		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
			backend.frame();
			GLFW.glfwSwapBuffers(window);
		}

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

}