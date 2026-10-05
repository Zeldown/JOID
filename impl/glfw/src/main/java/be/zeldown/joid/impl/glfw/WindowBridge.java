package be.zeldown.joid.impl.glfw;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import be.zeldown.joid.lib.bridge.window.IWindowBridge;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class WindowBridge implements IWindowBridge {

	private static final Map<Integer, Key> KEY_MAP  = new HashMap<>();
	private static final Map<Key, Integer> CODE_MAP = new EnumMap<>(Key.class);

	static {
		WindowBridge.map(Key.A, GLFW.GLFW_KEY_A);
		WindowBridge.map(Key.B, GLFW.GLFW_KEY_B);
		WindowBridge.map(Key.C, GLFW.GLFW_KEY_C);
		WindowBridge.map(Key.D, GLFW.GLFW_KEY_D);
		WindowBridge.map(Key.E, GLFW.GLFW_KEY_E);
		WindowBridge.map(Key.F, GLFW.GLFW_KEY_F);
		WindowBridge.map(Key.G, GLFW.GLFW_KEY_G);
		WindowBridge.map(Key.H, GLFW.GLFW_KEY_H);
		WindowBridge.map(Key.I, GLFW.GLFW_KEY_I);
		WindowBridge.map(Key.J, GLFW.GLFW_KEY_J);
		WindowBridge.map(Key.K, GLFW.GLFW_KEY_K);
		WindowBridge.map(Key.L, GLFW.GLFW_KEY_L);
		WindowBridge.map(Key.M, GLFW.GLFW_KEY_M);
		WindowBridge.map(Key.N, GLFW.GLFW_KEY_N);
		WindowBridge.map(Key.O, GLFW.GLFW_KEY_O);
		WindowBridge.map(Key.P, GLFW.GLFW_KEY_P);
		WindowBridge.map(Key.Q, GLFW.GLFW_KEY_Q);
		WindowBridge.map(Key.R, GLFW.GLFW_KEY_R);
		WindowBridge.map(Key.S, GLFW.GLFW_KEY_S);
		WindowBridge.map(Key.T, GLFW.GLFW_KEY_T);
		WindowBridge.map(Key.U, GLFW.GLFW_KEY_U);
		WindowBridge.map(Key.V, GLFW.GLFW_KEY_V);
		WindowBridge.map(Key.W, GLFW.GLFW_KEY_W);
		WindowBridge.map(Key.X, GLFW.GLFW_KEY_X);
		WindowBridge.map(Key.Y, GLFW.GLFW_KEY_Y);
		WindowBridge.map(Key.Z, GLFW.GLFW_KEY_Z);
		WindowBridge.map(Key.DIGIT_0, GLFW.GLFW_KEY_0);
		WindowBridge.map(Key.DIGIT_1, GLFW.GLFW_KEY_1);
		WindowBridge.map(Key.DIGIT_2, GLFW.GLFW_KEY_2);
		WindowBridge.map(Key.DIGIT_3, GLFW.GLFW_KEY_3);
		WindowBridge.map(Key.DIGIT_4, GLFW.GLFW_KEY_4);
		WindowBridge.map(Key.DIGIT_5, GLFW.GLFW_KEY_5);
		WindowBridge.map(Key.DIGIT_6, GLFW.GLFW_KEY_6);
		WindowBridge.map(Key.DIGIT_7, GLFW.GLFW_KEY_7);
		WindowBridge.map(Key.DIGIT_8, GLFW.GLFW_KEY_8);
		WindowBridge.map(Key.DIGIT_9, GLFW.GLFW_KEY_9);
		WindowBridge.map(Key.F1, GLFW.GLFW_KEY_F1);
		WindowBridge.map(Key.F2, GLFW.GLFW_KEY_F2);
		WindowBridge.map(Key.F3, GLFW.GLFW_KEY_F3);
		WindowBridge.map(Key.F4, GLFW.GLFW_KEY_F4);
		WindowBridge.map(Key.F5, GLFW.GLFW_KEY_F5);
		WindowBridge.map(Key.F6, GLFW.GLFW_KEY_F6);
		WindowBridge.map(Key.F7, GLFW.GLFW_KEY_F7);
		WindowBridge.map(Key.F8, GLFW.GLFW_KEY_F8);
		WindowBridge.map(Key.F9, GLFW.GLFW_KEY_F9);
		WindowBridge.map(Key.F10, GLFW.GLFW_KEY_F10);
		WindowBridge.map(Key.F11, GLFW.GLFW_KEY_F11);
		WindowBridge.map(Key.F12, GLFW.GLFW_KEY_F12);
		WindowBridge.map(Key.F13, GLFW.GLFW_KEY_F13);
		WindowBridge.map(Key.F14, GLFW.GLFW_KEY_F14);
		WindowBridge.map(Key.F15, GLFW.GLFW_KEY_F15);
		WindowBridge.map(Key.F16, GLFW.GLFW_KEY_F16);
		WindowBridge.map(Key.F17, GLFW.GLFW_KEY_F17);
		WindowBridge.map(Key.F18, GLFW.GLFW_KEY_F18);
		WindowBridge.map(Key.F19, GLFW.GLFW_KEY_F19);
		WindowBridge.map(Key.F20, GLFW.GLFW_KEY_F20);
		WindowBridge.map(Key.F21, GLFW.GLFW_KEY_F21);
		WindowBridge.map(Key.F22, GLFW.GLFW_KEY_F22);
		WindowBridge.map(Key.F23, GLFW.GLFW_KEY_F23);
		WindowBridge.map(Key.F24, GLFW.GLFW_KEY_F24);
		WindowBridge.map(Key.F25, GLFW.GLFW_KEY_F25);
		WindowBridge.map(Key.ESCAPE, GLFW.GLFW_KEY_ESCAPE);
		WindowBridge.map(Key.ENTER, GLFW.GLFW_KEY_ENTER);
		WindowBridge.map(Key.TAB, GLFW.GLFW_KEY_TAB);
		WindowBridge.map(Key.BACKSPACE, GLFW.GLFW_KEY_BACKSPACE);
		WindowBridge.map(Key.INSERT, GLFW.GLFW_KEY_INSERT);
		WindowBridge.map(Key.DELETE, GLFW.GLFW_KEY_DELETE);
		WindowBridge.map(Key.RIGHT, GLFW.GLFW_KEY_RIGHT);
		WindowBridge.map(Key.LEFT, GLFW.GLFW_KEY_LEFT);
		WindowBridge.map(Key.DOWN, GLFW.GLFW_KEY_DOWN);
		WindowBridge.map(Key.UP, GLFW.GLFW_KEY_UP);
		WindowBridge.map(Key.PAGE_UP, GLFW.GLFW_KEY_PAGE_UP);
		WindowBridge.map(Key.PAGE_DOWN, GLFW.GLFW_KEY_PAGE_DOWN);
		WindowBridge.map(Key.HOME, GLFW.GLFW_KEY_HOME);
		WindowBridge.map(Key.END, GLFW.GLFW_KEY_END);
		WindowBridge.map(Key.CAPS_LOCK, GLFW.GLFW_KEY_CAPS_LOCK);
		WindowBridge.map(Key.SCROLL_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK);
		WindowBridge.map(Key.NUM_LOCK, GLFW.GLFW_KEY_NUM_LOCK);
		WindowBridge.map(Key.PRINT_SCREEN, GLFW.GLFW_KEY_PRINT_SCREEN);
		WindowBridge.map(Key.PAUSE, GLFW.GLFW_KEY_PAUSE);
		WindowBridge.map(Key.SPACE, GLFW.GLFW_KEY_SPACE);
		WindowBridge.map(Key.APOSTROPHE, GLFW.GLFW_KEY_APOSTROPHE);
		WindowBridge.map(Key.COMMA, GLFW.GLFW_KEY_COMMA);
		WindowBridge.map(Key.MINUS, GLFW.GLFW_KEY_MINUS);
		WindowBridge.map(Key.PERIOD, GLFW.GLFW_KEY_PERIOD);
		WindowBridge.map(Key.SLASH, GLFW.GLFW_KEY_SLASH);
		WindowBridge.map(Key.SEMICOLON, GLFW.GLFW_KEY_SEMICOLON);
		WindowBridge.map(Key.EQUAL, GLFW.GLFW_KEY_EQUAL);
		WindowBridge.map(Key.LEFT_BRACKET, GLFW.GLFW_KEY_LEFT_BRACKET);
		WindowBridge.map(Key.BACKSLASH, GLFW.GLFW_KEY_BACKSLASH);
		WindowBridge.map(Key.RIGHT_BRACKET, GLFW.GLFW_KEY_RIGHT_BRACKET);
		WindowBridge.map(Key.GRAVE_ACCENT, GLFW.GLFW_KEY_GRAVE_ACCENT);
		WindowBridge.map(Key.NUMPAD_0, GLFW.GLFW_KEY_KP_0);
		WindowBridge.map(Key.NUMPAD_1, GLFW.GLFW_KEY_KP_1);
		WindowBridge.map(Key.NUMPAD_2, GLFW.GLFW_KEY_KP_2);
		WindowBridge.map(Key.NUMPAD_3, GLFW.GLFW_KEY_KP_3);
		WindowBridge.map(Key.NUMPAD_4, GLFW.GLFW_KEY_KP_4);
		WindowBridge.map(Key.NUMPAD_5, GLFW.GLFW_KEY_KP_5);
		WindowBridge.map(Key.NUMPAD_6, GLFW.GLFW_KEY_KP_6);
		WindowBridge.map(Key.NUMPAD_7, GLFW.GLFW_KEY_KP_7);
		WindowBridge.map(Key.NUMPAD_8, GLFW.GLFW_KEY_KP_8);
		WindowBridge.map(Key.NUMPAD_9, GLFW.GLFW_KEY_KP_9);
		WindowBridge.map(Key.NUMPAD_DECIMAL, GLFW.GLFW_KEY_KP_DECIMAL);
		WindowBridge.map(Key.NUMPAD_DIVIDE, GLFW.GLFW_KEY_KP_DIVIDE);
		WindowBridge.map(Key.NUMPAD_MULTIPLY, GLFW.GLFW_KEY_KP_MULTIPLY);
		WindowBridge.map(Key.NUMPAD_SUBTRACT, GLFW.GLFW_KEY_KP_SUBTRACT);
		WindowBridge.map(Key.NUMPAD_ADD, GLFW.GLFW_KEY_KP_ADD);
		WindowBridge.map(Key.NUMPAD_ENTER, GLFW.GLFW_KEY_KP_ENTER);
		WindowBridge.map(Key.NUMPAD_EQUAL, GLFW.GLFW_KEY_KP_EQUAL);
		WindowBridge.map(Key.LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
		WindowBridge.map(Key.LEFT_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL);
		WindowBridge.map(Key.LEFT_ALT, GLFW.GLFW_KEY_LEFT_ALT);
		WindowBridge.map(Key.LEFT_SUPER, GLFW.GLFW_KEY_LEFT_SUPER);
		WindowBridge.map(Key.RIGHT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
		WindowBridge.map(Key.RIGHT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
		WindowBridge.map(Key.RIGHT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
		WindowBridge.map(Key.RIGHT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
		WindowBridge.map(Key.MENU, GLFW.GLFW_KEY_MENU);
	}

	private final long window;

	public WindowBridge(final long window) {
		this.window = window;
	}

	@Override
	public int getWidth() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		return width[0];
	}

	@Override
	public int getHeight() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		return height[0];
	}

	@Override
	public double getMouseX() {
		final double[] x = new double[1];
		final double[] y = new double[1];
		GLFW.glfwGetCursorPos(this.window, x, y);

		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetWindowSize(this.window, width, height);
		return width[0] == 0 ? x[0] : x[0] * this.getWidth() / width[0];
	}

	@Override
	public double getMouseY() {
		final double[] x = new double[1];
		final double[] y = new double[1];
		GLFW.glfwGetCursorPos(this.window, x, y);

		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetWindowSize(this.window, width, height);
		return height[0] == 0 ? y[0] : y[0] * this.getHeight() / height[0];
	}

	@Override
	public boolean isMouseGrabbed() {
		return GLFW.glfwGetInputMode(this.window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED;
	}

	@Override
	public @NonNull String getClipboard() {
		final String clipboard = GLFW.glfwGetClipboardString(this.window);
		return clipboard == null ? "" : clipboard;
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		final Integer code = WindowBridge.CODE_MAP.get(key);
		return code != null && GLFW.glfwGetKey(this.window, code) == GLFW.GLFW_PRESS;
	}

	public static @NonNull Key getKey(final int code) {
		return WindowBridge.KEY_MAP.getOrDefault(code, Key.UNKNOWN);
	}

	@Override
	public void setClipboard(final @NonNull String text) {
		GLFW.glfwSetClipboardString(this.window, text);
	}

	private static void map(final Key key, final int code) {
		WindowBridge.CODE_MAP.put(key, code);
		WindowBridge.KEY_MAP.put(code, key);
	}

}