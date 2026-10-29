package dev.joid.base.lwjgl3.glfw.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwInputForwarder {

	public static void attach(final long window) {
		final GLFWCharCallback character = GLFW.glfwSetCharCallback(window, null);
		final GLFWCharModsCallback characterMods = GLFW.glfwSetCharModsCallback(window, null);
		if (characterMods == null) {
			GLFW.glfwSetCharCallback(window, (handle, codepoint) -> {
				if (!BridgeHandler.UI.charTyped(codepoint) && character != null) {
					character.invoke(handle, codepoint);
				}
			});
		} else {
			final boolean[] consumed = new boolean[1];
			GLFW.glfwSetCharModsCallback(window, (handle, codepoint, mods) -> {
				consumed[0] = BridgeHandler.UI.charTyped(codepoint);
				if (!consumed[0]) {
					characterMods.invoke(handle, codepoint, mods);
				}
			});
			if (character != null) {
				GLFW.glfwSetCharCallback(window, (handle, codepoint) -> {
					if (!consumed[0]) {
						character.invoke(handle, codepoint);
					}
				});
			}
		}

		final GLFWCursorPosCallback cursor = GLFW.glfwSetCursorPosCallback(window, null);
		GLFW.glfwSetCursorPosCallback(window, (handle, x, y) -> {
			if (!BridgeHandler.UI.mouseMoved() && cursor != null) {
				cursor.invoke(handle, x, y);
			}
		});

		final GLFWScrollCallback scroll = GLFW.glfwSetScrollCallback(window, null);
		GLFW.glfwSetScrollCallback(window, (handle, x, y) -> {
			if (!BridgeHandler.UI.mouseScroll(x, y) && scroll != null) {
				scroll.invoke(handle, x, y);
			}
		});

		final GLFWKeyCallback key = GLFW.glfwSetKeyCallback(window, null);
		GLFW.glfwSetKeyCallback(window, (handle, code, scancode, action, mods) -> {
			if (!GlfwInputForwarder.forwardKey(code, action) && key != null) {
				key.invoke(handle, code, scancode, action, mods);
			}
		});

		final GLFWMouseButtonCallback button = GLFW.glfwSetMouseButtonCallback(window, null);
		GLFW.glfwSetMouseButtonCallback(window, (handle, code, action, mods) -> {
			if (!GlfwInputForwarder.forwardMouseButton(code, action) && button != null) {
				button.invoke(handle, code, action, mods);
			}
		});
	}

	public static boolean forwardKey(final int code, final int action) {
		final Key physicalKey = GlfwKeys.getPhysicalKey(code);
		return action == GLFW.GLFW_PRESS ? BridgeHandler.UI.keyPressed(physicalKey, GlfwKeys.getKey(code)) : action == GLFW.GLFW_RELEASE ? BridgeHandler.UI.keyReleased(physicalKey) : BridgeHandler.UI.isKeyConsumed(physicalKey);
	}

	public static boolean forwardMouseButton(final int button, final int action) {
		return action == GLFW.GLFW_PRESS ? BridgeHandler.UI.mousePressed(MouseButton.of(button)) : BridgeHandler.UI.mouseReleased(MouseButton.of(button));
	}

}