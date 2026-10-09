package dev.joid.base.glfw.input;

import org.lwjgl.glfw.GLFW;

import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwInputForwarder {

	private final UIBridge bridge;

	public static @NonNull GlfwInputForwarder create(final @NonNull UIBridge bridge) {
		return new GlfwInputForwarder(bridge);
	}

	public @NonNull GlfwInputForwarder attach(final long window) {
		GLFW.glfwSetCharCallback(window, (handle, codepoint) -> this.charTyped(codepoint));
		GLFW.glfwSetCursorPosCallback(window, (handle, x, y) -> this.mouseMoved());
		GLFW.glfwSetScrollCallback(window, (handle, x, y) -> this.mouseScrolled(x, y));
		GLFW.glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
			if (action != GLFW.GLFW_RELEASE) {
				this.keyPressed(key);
			}
		});
		GLFW.glfwSetMouseButtonCallback(window, (handle, button, action, mods) -> {
			if (action == GLFW.GLFW_PRESS) {
				this.mousePressed(button);
			} else {
				this.mouseReleased(button);
			}
		});
		return this;
	}

	public boolean keyPressed(final int code) {
		return this.bridge.keyPressed(GlfwKeys.getKey(code));
	}

	public boolean charTyped(final int codepoint) {
		return this.bridge.charTyped(codepoint);
	}

	public boolean mouseMoved() {
		return this.bridge.mouseMoved();
	}

	public boolean mousePressed(final int button) {
		return this.bridge.mousePressed(MouseButton.from(button));
	}

	public boolean mouseReleased(final int button) {
		return this.bridge.mouseReleased(MouseButton.from(button));
	}

	public boolean mouseScrolled(final double notchesX, final double notchesY) {
		return this.bridge.mouseScroll(notchesX, notchesY);
	}

}