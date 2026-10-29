package dev.joid.base.lwjgl3.glfw.input;

import java.util.function.BooleanSupplier;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
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

	private static long     eventWindow;
	private static double[] eventCursor;

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

		final double[] position = {Double.NaN, Double.NaN};
		final GLFWCursorEnterCallback enter = GLFW.glfwSetCursorEnterCallback(window, null);
		GLFW.glfwSetCursorEnterCallback(window, (handle, entered) -> {
			if (!entered) {
				position[0] = position[1] = Double.NaN;
			}

			if (enter != null) {
				enter.invoke(handle, entered);
			}
		});

		final GLFWCursorPosCallback cursor = GLFW.glfwSetCursorPosCallback(window, null);
		GLFW.glfwSetCursorPosCallback(window, (handle, x, y) -> {
			position[0] = x;
			position[1] = y;
			if (!GlfwInputForwarder.forwardMouseEvent(window, position, BridgeHandler.UI::mouseMoved) && cursor != null) {
				cursor.invoke(handle, x, y);
			}
		});

		final GLFWScrollCallback scroll = GLFW.glfwSetScrollCallback(window, null);
		GLFW.glfwSetScrollCallback(window, (handle, x, y) -> {
			if (!GlfwInputForwarder.forwardMouseEvent(window, position, () -> BridgeHandler.UI.mouseScroll(x, y)) && scroll != null) {
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
			if (!GlfwInputForwarder.forwardMouseEvent(window, position, () -> GlfwInputForwarder.forwardMouseButton(code, action)) && button != null) {
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

	public static double[] getEventCursor(final long window) {
		final double[] cursor = GlfwInputForwarder.eventCursor;
		return window == GlfwInputForwarder.eventWindow && cursor != null && !Double.isNaN(cursor[0]) ? cursor.clone() : null;
	}

	private static boolean forwardMouseEvent(final long window, final double[] cursor, final BooleanSupplier event) {
		GlfwInputForwarder.eventWindow = window;
		GlfwInputForwarder.eventCursor = cursor;
		try {
			return event.getAsBoolean();
		} finally {
			GlfwInputForwarder.eventWindow = 0L;
			GlfwInputForwarder.eventCursor = null;
		}
	}

}