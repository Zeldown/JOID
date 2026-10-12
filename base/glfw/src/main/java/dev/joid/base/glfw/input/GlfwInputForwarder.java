package dev.joid.base.glfw.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwInputForwarder {

	public static @NonNull GlfwInputForwarder create() {
		return new GlfwInputForwarder();
	}

	public @NonNull GlfwInputForwarder attach(final long window) {
		final GLFWCharCallback character = GLFW.glfwSetCharCallback(window, null);
		GLFW.glfwSetCharCallback(window, (handle, codepoint) -> {
			if (!this.charTyped(codepoint) && character != null) {
				character.invoke(handle, codepoint);
			}
		});

		final GLFWCursorPosCallback cursor = GLFW.glfwSetCursorPosCallback(window, null);
		GLFW.glfwSetCursorPosCallback(window, (handle, x, y) -> {
			if (!this.mouseMoved() && cursor != null) {
				cursor.invoke(handle, x, y);
			}
		});

		final GLFWScrollCallback scroll = GLFW.glfwSetScrollCallback(window, null);
		GLFW.glfwSetScrollCallback(window, (handle, x, y) -> {
			if (!this.mouseScrolled(x, y) && scroll != null) {
				scroll.invoke(handle, x, y);
			}
		});

		final GLFWKeyCallback key = GLFW.glfwSetKeyCallback(window, null);
		GLFW.glfwSetKeyCallback(window, (handle, code, scancode, action, mods) -> {
			final boolean consumed = action != GLFW.GLFW_RELEASE ? this.keyPressed(code) : this.keyReleased(code);
			if (!consumed && key != null) {
				key.invoke(handle, code, scancode, action, mods);
			}
		});

		final GLFWMouseButtonCallback button = GLFW.glfwSetMouseButtonCallback(window, null);
		GLFW.glfwSetMouseButtonCallback(window, (handle, code, action, mods) -> {
			final boolean consumed = action == GLFW.GLFW_PRESS ? this.mousePressed(code) : this.mouseReleased(code);
			if (!consumed && button != null) {
				button.invoke(handle, code, action, mods);
			}
		});
		return this;
	}

	public boolean keyPressed(final int code) {
		return BridgeHandler.UI.keyPressed(GlfwKeys.getKey(code));
	}

	public boolean keyReleased(final int code) {
		return BridgeHandler.UI.keyReleased(GlfwKeys.getKey(code));
	}

	public boolean charTyped(final int codepoint) {
		return BridgeHandler.UI.charTyped(codepoint);
	}

	public boolean mouseMoved() {
		return BridgeHandler.UI.mouseMoved();
	}

	public boolean mousePressed(final int button) {
		return BridgeHandler.UI.mousePressed(MouseButton.from(button));
	}

	public boolean mouseReleased(final int button) {
		return BridgeHandler.UI.mouseReleased(MouseButton.from(button));
	}

	public boolean mouseScrolled(final double notchesX, final double notchesY) {
		return BridgeHandler.UI.mouseScroll(notchesX, notchesY);
	}

}