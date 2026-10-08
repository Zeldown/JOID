package dev.joid.base.glfw.input;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;

import org.lwjgl.glfw.GLFW;

import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.key.KeyLayout;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwKeys {

	private static final Map<Integer, Key> KEY_MAP  = new HashMap<>();
	private static final Map<Key, Integer> CODE_MAP = new EnumMap<>(Key.class);
	private static final KeyLayout         LAYOUT   = KeyLayout.create(key -> GLFW.glfwGetKeyName(GlfwKeys.getCode(key), 0));

	static {
		GlfwKeys.map(Key.A, GLFW.GLFW_KEY_A);
		GlfwKeys.map(Key.B, GLFW.GLFW_KEY_B);
		GlfwKeys.map(Key.C, GLFW.GLFW_KEY_C);
		GlfwKeys.map(Key.D, GLFW.GLFW_KEY_D);
		GlfwKeys.map(Key.E, GLFW.GLFW_KEY_E);
		GlfwKeys.map(Key.F, GLFW.GLFW_KEY_F);
		GlfwKeys.map(Key.G, GLFW.GLFW_KEY_G);
		GlfwKeys.map(Key.H, GLFW.GLFW_KEY_H);
		GlfwKeys.map(Key.I, GLFW.GLFW_KEY_I);
		GlfwKeys.map(Key.J, GLFW.GLFW_KEY_J);
		GlfwKeys.map(Key.K, GLFW.GLFW_KEY_K);
		GlfwKeys.map(Key.L, GLFW.GLFW_KEY_L);
		GlfwKeys.map(Key.M, GLFW.GLFW_KEY_M);
		GlfwKeys.map(Key.N, GLFW.GLFW_KEY_N);
		GlfwKeys.map(Key.O, GLFW.GLFW_KEY_O);
		GlfwKeys.map(Key.P, GLFW.GLFW_KEY_P);
		GlfwKeys.map(Key.Q, GLFW.GLFW_KEY_Q);
		GlfwKeys.map(Key.R, GLFW.GLFW_KEY_R);
		GlfwKeys.map(Key.S, GLFW.GLFW_KEY_S);
		GlfwKeys.map(Key.T, GLFW.GLFW_KEY_T);
		GlfwKeys.map(Key.U, GLFW.GLFW_KEY_U);
		GlfwKeys.map(Key.V, GLFW.GLFW_KEY_V);
		GlfwKeys.map(Key.W, GLFW.GLFW_KEY_W);
		GlfwKeys.map(Key.X, GLFW.GLFW_KEY_X);
		GlfwKeys.map(Key.Y, GLFW.GLFW_KEY_Y);
		GlfwKeys.map(Key.Z, GLFW.GLFW_KEY_Z);
		GlfwKeys.map(Key.DIGIT_0, GLFW.GLFW_KEY_0);
		GlfwKeys.map(Key.DIGIT_1, GLFW.GLFW_KEY_1);
		GlfwKeys.map(Key.DIGIT_2, GLFW.GLFW_KEY_2);
		GlfwKeys.map(Key.DIGIT_3, GLFW.GLFW_KEY_3);
		GlfwKeys.map(Key.DIGIT_4, GLFW.GLFW_KEY_4);
		GlfwKeys.map(Key.DIGIT_5, GLFW.GLFW_KEY_5);
		GlfwKeys.map(Key.DIGIT_6, GLFW.GLFW_KEY_6);
		GlfwKeys.map(Key.DIGIT_7, GLFW.GLFW_KEY_7);
		GlfwKeys.map(Key.DIGIT_8, GLFW.GLFW_KEY_8);
		GlfwKeys.map(Key.DIGIT_9, GLFW.GLFW_KEY_9);
		GlfwKeys.map(Key.F1, GLFW.GLFW_KEY_F1);
		GlfwKeys.map(Key.F2, GLFW.GLFW_KEY_F2);
		GlfwKeys.map(Key.F3, GLFW.GLFW_KEY_F3);
		GlfwKeys.map(Key.F4, GLFW.GLFW_KEY_F4);
		GlfwKeys.map(Key.F5, GLFW.GLFW_KEY_F5);
		GlfwKeys.map(Key.F6, GLFW.GLFW_KEY_F6);
		GlfwKeys.map(Key.F7, GLFW.GLFW_KEY_F7);
		GlfwKeys.map(Key.F8, GLFW.GLFW_KEY_F8);
		GlfwKeys.map(Key.F9, GLFW.GLFW_KEY_F9);
		GlfwKeys.map(Key.F10, GLFW.GLFW_KEY_F10);
		GlfwKeys.map(Key.F11, GLFW.GLFW_KEY_F11);
		GlfwKeys.map(Key.F12, GLFW.GLFW_KEY_F12);
		GlfwKeys.map(Key.F13, GLFW.GLFW_KEY_F13);
		GlfwKeys.map(Key.F14, GLFW.GLFW_KEY_F14);
		GlfwKeys.map(Key.F15, GLFW.GLFW_KEY_F15);
		GlfwKeys.map(Key.F16, GLFW.GLFW_KEY_F16);
		GlfwKeys.map(Key.F17, GLFW.GLFW_KEY_F17);
		GlfwKeys.map(Key.F18, GLFW.GLFW_KEY_F18);
		GlfwKeys.map(Key.F19, GLFW.GLFW_KEY_F19);
		GlfwKeys.map(Key.F20, GLFW.GLFW_KEY_F20);
		GlfwKeys.map(Key.F21, GLFW.GLFW_KEY_F21);
		GlfwKeys.map(Key.F22, GLFW.GLFW_KEY_F22);
		GlfwKeys.map(Key.F23, GLFW.GLFW_KEY_F23);
		GlfwKeys.map(Key.F24, GLFW.GLFW_KEY_F24);
		GlfwKeys.map(Key.F25, GLFW.GLFW_KEY_F25);
		GlfwKeys.map(Key.ESCAPE, GLFW.GLFW_KEY_ESCAPE);
		GlfwKeys.map(Key.ENTER, GLFW.GLFW_KEY_ENTER);
		GlfwKeys.map(Key.TAB, GLFW.GLFW_KEY_TAB);
		GlfwKeys.map(Key.BACKSPACE, GLFW.GLFW_KEY_BACKSPACE);
		GlfwKeys.map(Key.INSERT, GLFW.GLFW_KEY_INSERT);
		GlfwKeys.map(Key.DELETE, GLFW.GLFW_KEY_DELETE);
		GlfwKeys.map(Key.RIGHT, GLFW.GLFW_KEY_RIGHT);
		GlfwKeys.map(Key.LEFT, GLFW.GLFW_KEY_LEFT);
		GlfwKeys.map(Key.DOWN, GLFW.GLFW_KEY_DOWN);
		GlfwKeys.map(Key.UP, GLFW.GLFW_KEY_UP);
		GlfwKeys.map(Key.PAGE_UP, GLFW.GLFW_KEY_PAGE_UP);
		GlfwKeys.map(Key.PAGE_DOWN, GLFW.GLFW_KEY_PAGE_DOWN);
		GlfwKeys.map(Key.HOME, GLFW.GLFW_KEY_HOME);
		GlfwKeys.map(Key.END, GLFW.GLFW_KEY_END);
		GlfwKeys.map(Key.CAPS_LOCK, GLFW.GLFW_KEY_CAPS_LOCK);
		GlfwKeys.map(Key.SCROLL_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK);
		GlfwKeys.map(Key.NUM_LOCK, GLFW.GLFW_KEY_NUM_LOCK);
		GlfwKeys.map(Key.PRINT_SCREEN, GLFW.GLFW_KEY_PRINT_SCREEN);
		GlfwKeys.map(Key.PAUSE, GLFW.GLFW_KEY_PAUSE);
		GlfwKeys.map(Key.SPACE, GLFW.GLFW_KEY_SPACE);
		GlfwKeys.map(Key.APOSTROPHE, GLFW.GLFW_KEY_APOSTROPHE);
		GlfwKeys.map(Key.COMMA, GLFW.GLFW_KEY_COMMA);
		GlfwKeys.map(Key.MINUS, GLFW.GLFW_KEY_MINUS);
		GlfwKeys.map(Key.PERIOD, GLFW.GLFW_KEY_PERIOD);
		GlfwKeys.map(Key.SLASH, GLFW.GLFW_KEY_SLASH);
		GlfwKeys.map(Key.SEMICOLON, GLFW.GLFW_KEY_SEMICOLON);
		GlfwKeys.map(Key.EQUAL, GLFW.GLFW_KEY_EQUAL);
		GlfwKeys.map(Key.LEFT_BRACKET, GLFW.GLFW_KEY_LEFT_BRACKET);
		GlfwKeys.map(Key.BACKSLASH, GLFW.GLFW_KEY_BACKSLASH);
		GlfwKeys.map(Key.RIGHT_BRACKET, GLFW.GLFW_KEY_RIGHT_BRACKET);
		GlfwKeys.map(Key.GRAVE_ACCENT, GLFW.GLFW_KEY_GRAVE_ACCENT);
		GlfwKeys.map(Key.NUMPAD_0, GLFW.GLFW_KEY_KP_0);
		GlfwKeys.map(Key.NUMPAD_1, GLFW.GLFW_KEY_KP_1);
		GlfwKeys.map(Key.NUMPAD_2, GLFW.GLFW_KEY_KP_2);
		GlfwKeys.map(Key.NUMPAD_3, GLFW.GLFW_KEY_KP_3);
		GlfwKeys.map(Key.NUMPAD_4, GLFW.GLFW_KEY_KP_4);
		GlfwKeys.map(Key.NUMPAD_5, GLFW.GLFW_KEY_KP_5);
		GlfwKeys.map(Key.NUMPAD_6, GLFW.GLFW_KEY_KP_6);
		GlfwKeys.map(Key.NUMPAD_7, GLFW.GLFW_KEY_KP_7);
		GlfwKeys.map(Key.NUMPAD_8, GLFW.GLFW_KEY_KP_8);
		GlfwKeys.map(Key.NUMPAD_9, GLFW.GLFW_KEY_KP_9);
		GlfwKeys.map(Key.NUMPAD_DECIMAL, GLFW.GLFW_KEY_KP_DECIMAL);
		GlfwKeys.map(Key.NUMPAD_DIVIDE, GLFW.GLFW_KEY_KP_DIVIDE);
		GlfwKeys.map(Key.NUMPAD_MULTIPLY, GLFW.GLFW_KEY_KP_MULTIPLY);
		GlfwKeys.map(Key.NUMPAD_SUBTRACT, GLFW.GLFW_KEY_KP_SUBTRACT);
		GlfwKeys.map(Key.NUMPAD_ADD, GLFW.GLFW_KEY_KP_ADD);
		GlfwKeys.map(Key.NUMPAD_ENTER, GLFW.GLFW_KEY_KP_ENTER);
		GlfwKeys.map(Key.NUMPAD_EQUAL, GLFW.GLFW_KEY_KP_EQUAL);
		GlfwKeys.map(Key.LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
		GlfwKeys.map(Key.LEFT_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL);
		GlfwKeys.map(Key.LEFT_ALT, GLFW.GLFW_KEY_LEFT_ALT);
		GlfwKeys.map(Key.LEFT_SUPER, GLFW.GLFW_KEY_LEFT_SUPER);
		GlfwKeys.map(Key.RIGHT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
		GlfwKeys.map(Key.RIGHT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
		GlfwKeys.map(Key.RIGHT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
		GlfwKeys.map(Key.RIGHT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
		GlfwKeys.map(Key.MENU, GLFW.GLFW_KEY_MENU);
	}

	public static int getCode(final @NonNull Key key) {
		return GlfwKeys.CODE_MAP.getOrDefault(key, GLFW.GLFW_KEY_UNKNOWN);
	}

	public static @NonNull Key getKey(final int code) {
		return GlfwKeys.LAYOUT.translate(GlfwKeys.getPhysicalKey(code));
	}

	public static @NonNull Key getPhysicalKey(final int code) {
		return GlfwKeys.KEY_MAP.getOrDefault(code, Key.UNKNOWN);
	}

	public static boolean isKeyDown(final @NonNull Key key, final @NonNull IntPredicate down) {
		return GlfwKeys.LAYOUT.isDown(key, physical -> GlfwKeys.isPhysicalKeyDown(physical, down));
	}

	public static boolean isPhysicalKeyDown(final @NonNull Key key, final @NonNull IntPredicate down) {
		final int code = GlfwKeys.getCode(key);
		return code != GLFW.GLFW_KEY_UNKNOWN && down.test(code);
	}

	private static void map(final Key key, final int code) {
		GlfwKeys.CODE_MAP.put(key, code);
		GlfwKeys.KEY_MAP.put(code, key);
	}

}