package dev.joid.base.glfw.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import dev.joid.base.glfw.GlfwWindowBridge;
import dev.joid.base.glfw.input.GlfwInputForwarder;
import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.Getter;

public abstract class GlfwDemoWindow extends DemoUIBridge {

	@Getter
	private final long window;

	private final GlfwInputForwarder input;

	protected GlfwDemoWindow() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		this.configureWindow();
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		this.window = GLFW.glfwCreateWindow(1920, 1080, "JOID - Demo (" + this.getBackendName() + ")", 0L, 0L);
		if (this.window == 0L) {
			throw new IllegalStateException("Unable to create the GLFW window");
		}

		this.registerBackend(this.window);

		this.input = GlfwInputForwarder.create(this).attach(this.window);

		GLFW.glfwSetFramebufferSizeCallback(this.window, (handle, width, height) -> this.onResize(width, height));
		super.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
	}

	public void run() {
		super.start();
		this.loop();
	}

	public void loop() {
		while (!GLFW.glfwWindowShouldClose(this.window)) {
			if (GLFW.glfwGetWindowAttrib(this.window, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
				GLFW.glfwWaitEvents();
				continue;
			}

			GLFW.glfwPollEvents();
			this.input.flush();

			super.frame();
			this.present();
		}

		BridgeHandler.WINDOW.getBridge(GlfwWindowBridge.class).destroy();
		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
		System.exit(0);
	}

	protected abstract String getBackendName();

	protected abstract void configureWindow();

	protected abstract void registerBackend(final long window);

	protected abstract void present();

	private void onResize(final int width, final int height) {
		if (width == 0 || height == 0) {
			return;
		}

		super.resize(width, height);
	}

}