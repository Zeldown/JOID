package dev.joid.impl.glfw.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.impl.glfw.WindowBridge;
import dev.joid.impl.glfw.input.KeyCharacterMerger;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
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
		this.identity();
	}

	public void init() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	public void run() {
		this.init();
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

			super.update();
			this.render();
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

	private void identity() {
		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.ortho(0D, windowBridge.getWidth(), windowBridge.getHeight(), 0D, 0D, 10000D);
		render.viewport(0, 0, windowBridge.getWidth(), windowBridge.getHeight());
	}

	private void render() {
		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.beginFrame();
		render.clear(0F, 0F, 0F, 0F);
		DrawUtils.SHAPE.drawRect(0, 0, windowBridge.getWidth(), windowBridge.getHeight(), new Color(50, 50, 50));
		super.draw();
		render.endFrame();
	}

	private void registerCallbacks() {
		GLFW.glfwSetCharCallback(this.window, (handle, codepoint) -> this.keyMerger.charTyped(codepoint));
		GLFW.glfwSetCursorPosCallback(this.window, (handle, x, y) -> super.mouseMoved());
		GLFW.glfwSetScrollCallback(this.window, (handle, x, y) -> super.mouseScroll(y));
		GLFW.glfwSetKeyCallback(this.window, (handle, key, scancode, action, mods) -> this.onKey(key, action, mods));
		GLFW.glfwSetFramebufferSizeCallback(this.window, (handle, width, height) -> this.onResize());
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

	private void onResize() {
		if (BridgeHandler.WINDOW.get().getWidth() == 0 || BridgeHandler.WINDOW.get().getHeight() == 0) {
			return;
		}

		this.identity();
		super.load();
	}

}