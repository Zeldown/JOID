package dev.joid.base.glfw.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import dev.joid.base.glfw.WindowBridge;
import dev.joid.base.glfw.input.KeyCharacterMerger;
import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.click.ClickType;
import lombok.Getter;

public abstract class DemoWindow extends DemoUIBridge {

	@Getter
	private final long window;

	private final KeyCharacterMerger keyMerger;

	protected DemoWindow() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		this.configureWindow();
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		this.window = GLFW.glfwCreateWindow(1920, 1080, "JOID - Demo (" + this.getEngineName() + ")", 0L, 0L);
		if (this.window == 0L) {
			throw new IllegalStateException("Unable to create the GLFW window");
		}

		this.registerBackend(this.window);

		this.keyMerger = KeyCharacterMerger.create(super::keyTyped);

		this.registerCallbacks();
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
			this.keyMerger.flush();

			super.frame();
			this.present();
		}

		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
		System.exit(0);
	}

	protected abstract String getEngineName();

	protected abstract void configureWindow();

	protected abstract void registerBackend(final long window);

	protected abstract void present();

	private void registerCallbacks() {
		GLFW.glfwSetCharCallback(this.window, (handle, codepoint) -> this.keyMerger.charTyped(codepoint));
		GLFW.glfwSetCursorPosCallback(this.window, (handle, x, y) -> super.mouseMoved());
		GLFW.glfwSetScrollCallback(this.window, (handle, x, y) -> super.mouseScroll(y));
		GLFW.glfwSetKeyCallback(this.window, (handle, key, scancode, action, mods) -> this.onKey(key, action, mods));
		GLFW.glfwSetFramebufferSizeCallback(this.window, (handle, width, height) -> this.onResize(width, height));
		GLFW.glfwSetMouseButtonCallback(this.window, (handle, button, action, mods) -> this.onMouseButton(button, action));
	}

	private void onKey(final int code, final int action, final int mods) {
		if (action == GLFW.GLFW_RELEASE) {
			return;
		}

		this.keyMerger.keyPressed(WindowBridge.getKey(code), code, mods);
	}

	private void onMouseButton(final int button, final int action) {
		if (action == GLFW.GLFW_PRESS) {
			super.mousePressed(ClickType.from(button));
		} else {
			super.mouseReleased(ClickType.from(button));
		}
	}

	private void onResize(final int width, final int height) {
		if (width == 0 || height == 0) {
			return;
		}

		super.resize(width, height);
	}

}