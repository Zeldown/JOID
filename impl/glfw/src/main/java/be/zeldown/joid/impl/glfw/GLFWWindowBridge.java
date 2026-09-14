package be.zeldown.joid.impl.glfw;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import be.zeldown.joid.lib.bridge.window.IWindowBridge;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class GLFWWindowBridge implements IWindowBridge {

	private static final Map<Key, Integer> CODE_MAP = new EnumMap<>(Key.class);
	private static final Map<Integer, Key> KEY_MAP  = new HashMap<>();

	static {
		GLFWWindowBridge.map(Key.A, GLFW.GLFW_KEY_A);
		GLFWWindowBridge.map(Key.B, GLFW.GLFW_KEY_B);
		GLFWWindowBridge.map(Key.C, GLFW.GLFW_KEY_C);
		GLFWWindowBridge.map(Key.D, GLFW.GLFW_KEY_D);
		GLFWWindowBridge.map(Key.E, GLFW.GLFW_KEY_E);
		GLFWWindowBridge.map(Key.F, GLFW.GLFW_KEY_F);
		GLFWWindowBridge.map(Key.G, GLFW.GLFW_KEY_G);
		GLFWWindowBridge.map(Key.H, GLFW.GLFW_KEY_H);
		GLFWWindowBridge.map(Key.I, GLFW.GLFW_KEY_I);
		GLFWWindowBridge.map(Key.J, GLFW.GLFW_KEY_J);
		GLFWWindowBridge.map(Key.K, GLFW.GLFW_KEY_K);
		GLFWWindowBridge.map(Key.L, GLFW.GLFW_KEY_L);
		GLFWWindowBridge.map(Key.M, GLFW.GLFW_KEY_M);
		GLFWWindowBridge.map(Key.N, GLFW.GLFW_KEY_N);
		GLFWWindowBridge.map(Key.O, GLFW.GLFW_KEY_O);
		GLFWWindowBridge.map(Key.P, GLFW.GLFW_KEY_P);
		GLFWWindowBridge.map(Key.Q, GLFW.GLFW_KEY_Q);
		GLFWWindowBridge.map(Key.R, GLFW.GLFW_KEY_R);
		GLFWWindowBridge.map(Key.S, GLFW.GLFW_KEY_S);
		GLFWWindowBridge.map(Key.T, GLFW.GLFW_KEY_T);
		GLFWWindowBridge.map(Key.U, GLFW.GLFW_KEY_U);
		GLFWWindowBridge.map(Key.V, GLFW.GLFW_KEY_V);
		GLFWWindowBridge.map(Key.W, GLFW.GLFW_KEY_W);
		GLFWWindowBridge.map(Key.X, GLFW.GLFW_KEY_X);
		GLFWWindowBridge.map(Key.Y, GLFW.GLFW_KEY_Y);
		GLFWWindowBridge.map(Key.Z, GLFW.GLFW_KEY_Z);
		GLFWWindowBridge.map(Key.DIGIT_0, GLFW.GLFW_KEY_0);
		GLFWWindowBridge.map(Key.DIGIT_1, GLFW.GLFW_KEY_1);
		GLFWWindowBridge.map(Key.DIGIT_2, GLFW.GLFW_KEY_2);
		GLFWWindowBridge.map(Key.DIGIT_3, GLFW.GLFW_KEY_3);
		GLFWWindowBridge.map(Key.DIGIT_4, GLFW.GLFW_KEY_4);
		GLFWWindowBridge.map(Key.DIGIT_5, GLFW.GLFW_KEY_5);
		GLFWWindowBridge.map(Key.DIGIT_6, GLFW.GLFW_KEY_6);
		GLFWWindowBridge.map(Key.DIGIT_7, GLFW.GLFW_KEY_7);
		GLFWWindowBridge.map(Key.DIGIT_8, GLFW.GLFW_KEY_8);
		GLFWWindowBridge.map(Key.DIGIT_9, GLFW.GLFW_KEY_9);
		GLFWWindowBridge.map(Key.F1, GLFW.GLFW_KEY_F1);
		GLFWWindowBridge.map(Key.F2, GLFW.GLFW_KEY_F2);
		GLFWWindowBridge.map(Key.F3, GLFW.GLFW_KEY_F3);
		GLFWWindowBridge.map(Key.F4, GLFW.GLFW_KEY_F4);
		GLFWWindowBridge.map(Key.F5, GLFW.GLFW_KEY_F5);
		GLFWWindowBridge.map(Key.F6, GLFW.GLFW_KEY_F6);
		GLFWWindowBridge.map(Key.F7, GLFW.GLFW_KEY_F7);
		GLFWWindowBridge.map(Key.F8, GLFW.GLFW_KEY_F8);
		GLFWWindowBridge.map(Key.F9, GLFW.GLFW_KEY_F9);
		GLFWWindowBridge.map(Key.F10, GLFW.GLFW_KEY_F10);
		GLFWWindowBridge.map(Key.F11, GLFW.GLFW_KEY_F11);
		GLFWWindowBridge.map(Key.F12, GLFW.GLFW_KEY_F12);
		GLFWWindowBridge.map(Key.F13, GLFW.GLFW_KEY_F13);
		GLFWWindowBridge.map(Key.F14, GLFW.GLFW_KEY_F14);
		GLFWWindowBridge.map(Key.F15, GLFW.GLFW_KEY_F15);
		GLFWWindowBridge.map(Key.F16, GLFW.GLFW_KEY_F16);
		GLFWWindowBridge.map(Key.F17, GLFW.GLFW_KEY_F17);
		GLFWWindowBridge.map(Key.F18, GLFW.GLFW_KEY_F18);
		GLFWWindowBridge.map(Key.F19, GLFW.GLFW_KEY_F19);
		GLFWWindowBridge.map(Key.F20, GLFW.GLFW_KEY_F20);
		GLFWWindowBridge.map(Key.F21, GLFW.GLFW_KEY_F21);
		GLFWWindowBridge.map(Key.F22, GLFW.GLFW_KEY_F22);
		GLFWWindowBridge.map(Key.F23, GLFW.GLFW_KEY_F23);
		GLFWWindowBridge.map(Key.F24, GLFW.GLFW_KEY_F24);
		GLFWWindowBridge.map(Key.F25, GLFW.GLFW_KEY_F25);
		GLFWWindowBridge.map(Key.ESCAPE, GLFW.GLFW_KEY_ESCAPE);
		GLFWWindowBridge.map(Key.ENTER, GLFW.GLFW_KEY_ENTER);
		GLFWWindowBridge.map(Key.TAB, GLFW.GLFW_KEY_TAB);
		GLFWWindowBridge.map(Key.BACKSPACE, GLFW.GLFW_KEY_BACKSPACE);
		GLFWWindowBridge.map(Key.INSERT, GLFW.GLFW_KEY_INSERT);
		GLFWWindowBridge.map(Key.DELETE, GLFW.GLFW_KEY_DELETE);
		GLFWWindowBridge.map(Key.RIGHT, GLFW.GLFW_KEY_RIGHT);
		GLFWWindowBridge.map(Key.LEFT, GLFW.GLFW_KEY_LEFT);
		GLFWWindowBridge.map(Key.DOWN, GLFW.GLFW_KEY_DOWN);
		GLFWWindowBridge.map(Key.UP, GLFW.GLFW_KEY_UP);
		GLFWWindowBridge.map(Key.PAGE_UP, GLFW.GLFW_KEY_PAGE_UP);
		GLFWWindowBridge.map(Key.PAGE_DOWN, GLFW.GLFW_KEY_PAGE_DOWN);
		GLFWWindowBridge.map(Key.HOME, GLFW.GLFW_KEY_HOME);
		GLFWWindowBridge.map(Key.END, GLFW.GLFW_KEY_END);
		GLFWWindowBridge.map(Key.CAPS_LOCK, GLFW.GLFW_KEY_CAPS_LOCK);
		GLFWWindowBridge.map(Key.SCROLL_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK);
		GLFWWindowBridge.map(Key.NUM_LOCK, GLFW.GLFW_KEY_NUM_LOCK);
		GLFWWindowBridge.map(Key.PRINT_SCREEN, GLFW.GLFW_KEY_PRINT_SCREEN);
		GLFWWindowBridge.map(Key.PAUSE, GLFW.GLFW_KEY_PAUSE);
		GLFWWindowBridge.map(Key.SPACE, GLFW.GLFW_KEY_SPACE);
		GLFWWindowBridge.map(Key.APOSTROPHE, GLFW.GLFW_KEY_APOSTROPHE);
		GLFWWindowBridge.map(Key.COMMA, GLFW.GLFW_KEY_COMMA);
		GLFWWindowBridge.map(Key.MINUS, GLFW.GLFW_KEY_MINUS);
		GLFWWindowBridge.map(Key.PERIOD, GLFW.GLFW_KEY_PERIOD);
		GLFWWindowBridge.map(Key.SLASH, GLFW.GLFW_KEY_SLASH);
		GLFWWindowBridge.map(Key.SEMICOLON, GLFW.GLFW_KEY_SEMICOLON);
		GLFWWindowBridge.map(Key.EQUAL, GLFW.GLFW_KEY_EQUAL);
		GLFWWindowBridge.map(Key.LEFT_BRACKET, GLFW.GLFW_KEY_LEFT_BRACKET);
		GLFWWindowBridge.map(Key.BACKSLASH, GLFW.GLFW_KEY_BACKSLASH);
		GLFWWindowBridge.map(Key.RIGHT_BRACKET, GLFW.GLFW_KEY_RIGHT_BRACKET);
		GLFWWindowBridge.map(Key.GRAVE_ACCENT, GLFW.GLFW_KEY_GRAVE_ACCENT);
		GLFWWindowBridge.map(Key.NUMPAD_0, GLFW.GLFW_KEY_KP_0);
		GLFWWindowBridge.map(Key.NUMPAD_1, GLFW.GLFW_KEY_KP_1);
		GLFWWindowBridge.map(Key.NUMPAD_2, GLFW.GLFW_KEY_KP_2);
		GLFWWindowBridge.map(Key.NUMPAD_3, GLFW.GLFW_KEY_KP_3);
		GLFWWindowBridge.map(Key.NUMPAD_4, GLFW.GLFW_KEY_KP_4);
		GLFWWindowBridge.map(Key.NUMPAD_5, GLFW.GLFW_KEY_KP_5);
		GLFWWindowBridge.map(Key.NUMPAD_6, GLFW.GLFW_KEY_KP_6);
		GLFWWindowBridge.map(Key.NUMPAD_7, GLFW.GLFW_KEY_KP_7);
		GLFWWindowBridge.map(Key.NUMPAD_8, GLFW.GLFW_KEY_KP_8);
		GLFWWindowBridge.map(Key.NUMPAD_9, GLFW.GLFW_KEY_KP_9);
		GLFWWindowBridge.map(Key.NUMPAD_DECIMAL, GLFW.GLFW_KEY_KP_DECIMAL);
		GLFWWindowBridge.map(Key.NUMPAD_DIVIDE, GLFW.GLFW_KEY_KP_DIVIDE);
		GLFWWindowBridge.map(Key.NUMPAD_MULTIPLY, GLFW.GLFW_KEY_KP_MULTIPLY);
		GLFWWindowBridge.map(Key.NUMPAD_SUBTRACT, GLFW.GLFW_KEY_KP_SUBTRACT);
		GLFWWindowBridge.map(Key.NUMPAD_ADD, GLFW.GLFW_KEY_KP_ADD);
		GLFWWindowBridge.map(Key.NUMPAD_ENTER, GLFW.GLFW_KEY_KP_ENTER);
		GLFWWindowBridge.map(Key.NUMPAD_EQUAL, GLFW.GLFW_KEY_KP_EQUAL);
		GLFWWindowBridge.map(Key.LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
		GLFWWindowBridge.map(Key.LEFT_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL);
		GLFWWindowBridge.map(Key.LEFT_ALT, GLFW.GLFW_KEY_LEFT_ALT);
		GLFWWindowBridge.map(Key.LEFT_SUPER, GLFW.GLFW_KEY_LEFT_SUPER);
		GLFWWindowBridge.map(Key.RIGHT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
		GLFWWindowBridge.map(Key.RIGHT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
		GLFWWindowBridge.map(Key.RIGHT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
		GLFWWindowBridge.map(Key.RIGHT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
		GLFWWindowBridge.map(Key.MENU, GLFW.GLFW_KEY_MENU);
	}

	private final long window;

	public GLFWWindowBridge(final long window) {
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
	public boolean isKeyDown(final @NonNull Key key) {
		final Integer code = GLFWWindowBridge.CODE_MAP.get(key);
		return code != null && GLFW.glfwGetKey(this.window, code) == GLFW.GLFW_PRESS;
	}

	@Override
	public @NonNull String getClipboard() {
		final String clipboard = GLFW.glfwGetClipboardString(this.window);
		return clipboard == null ? "" : clipboard;
	}

	@Override
	public void setClipboard(final @NonNull String text) {
		GLFW.glfwSetClipboardString(this.window, text);
	}

	public static @NonNull Key getKey(final int code) {
		return GLFWWindowBridge.KEY_MAP.getOrDefault(code, Key.UNKNOWN);
	}

	private static void map(final Key key, final int code) {
		GLFWWindowBridge.CODE_MAP.put(key, code);
		GLFWWindowBridge.KEY_MAP.put(code, key);
	}

}