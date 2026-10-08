package dev.joid.backend.lwjgl3.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.Backend;
import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

public class DemoWindow extends dev.joid.base.glfw.demo.DemoWindow {

	public static void main(final String[] args) {
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	@Override
	protected String getEngineName() {
		return "LWJGL 3";
	}

	@Override
	protected void configureWindow() {
		GlContextRequest.CORE_33.apply();
	}

	@Override
	protected void registerBackend(final long window) {
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		Backend.register(window);
	}

	@Override
	protected void present() {
		GLFW.glfwSwapBuffers(super.getWindow());
	}

}