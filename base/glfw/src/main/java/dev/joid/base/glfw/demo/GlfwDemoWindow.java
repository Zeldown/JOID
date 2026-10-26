package dev.joid.base.glfw.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import dev.joid.base.glfw.GlfwWindowBridge;
import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.backend.Backend;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GlfwDemoWindow extends DemoUIBridge {

	@Getter
	private long window;

	private Backend backend;

	public void run() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("GLFW cannot be initialized, check that the system has a display (DISPLAY or WAYLAND_DISPLAY on Linux)");
		}

		this.configureWindow();
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		this.window = GLFW.glfwCreateWindow(1920, 1080, "JOID - Demo (" + this.getBackendName() + ")", 0L, 0L);
		if (this.window == 0L) {
			throw new IllegalStateException("GLFW cannot create the 1920x1080 demo window of " + this.getBackendName() + ", check that the graphics driver supports the graphics API the window asks for (its OpenGL version or Vulkan)");
		}

		JOID.onReady(() -> BridgeHandler.UI.register(this));
		this.backend = this.registerBackend(this.window);

		super.start();
		this.loop();
	}

	protected abstract String getBackendName();

	protected abstract void configureWindow();

	protected abstract @NonNull Backend registerBackend(final long window);

	protected abstract void present();

	protected void destroyBackend() {}

	private void loop() {
		while (!GLFW.glfwWindowShouldClose(this.window)) {
			if (GLFW.glfwGetWindowAttrib(this.window, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
				GLFW.glfwWaitEvents();
				continue;
			}

			GLFW.glfwPollEvents();

			this.backend.frame();
			this.present();
		}

		this.destroyBackend();
		BridgeHandler.WINDOW.getBridge(GlfwWindowBridge.class).destroy();
		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
		System.exit(0);
	}

}