package dev.joid.backend.lwjgl3.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.base.glfw.demo.GlfwDemoWindow;
import dev.joid.lib.bridge.backend.Backend;
import lombok.NonNull;

public class DemoWindow extends GlfwDemoWindow {

	public static void main(final String[] args) {
		new DemoWindow().run();
	}

	@Override
	protected String getBackendName() {
		return "LWJGL 3";
	}

	@Override
	protected void configureWindow() {
		GlContextRequest.CORE_33.apply();
	}

	@Override
	protected @NonNull Backend registerBackend(final long window) {
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		return Lwjgl3Backend.create(window).register();
	}

	@Override
	protected void present() {
		GLFW.glfwSwapBuffers(super.getWindow());
	}

}