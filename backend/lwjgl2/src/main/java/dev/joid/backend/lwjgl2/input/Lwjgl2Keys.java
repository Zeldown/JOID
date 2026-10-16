package dev.joid.backend.lwjgl2.input;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.KeyLayout;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2Keys {

	private static final Key[]             KEY_ARRAY = new Key[Keyboard.KEYBOARD_SIZE];
	private static final Map<Key, Integer> CODE_MAP  = new EnumMap<>(Key.class);

	static {
		Arrays.fill(Lwjgl2Keys.KEY_ARRAY, Key.UNKNOWN);
		Lwjgl2Keys.map(Key.A, Keyboard.KEY_A);
		Lwjgl2Keys.map(Key.B, Keyboard.KEY_B);
		Lwjgl2Keys.map(Key.C, Keyboard.KEY_C);
		Lwjgl2Keys.map(Key.D, Keyboard.KEY_D);
		Lwjgl2Keys.map(Key.E, Keyboard.KEY_E);
		Lwjgl2Keys.map(Key.F, Keyboard.KEY_F);
		Lwjgl2Keys.map(Key.G, Keyboard.KEY_G);
		Lwjgl2Keys.map(Key.H, Keyboard.KEY_H);
		Lwjgl2Keys.map(Key.I, Keyboard.KEY_I);
		Lwjgl2Keys.map(Key.J, Keyboard.KEY_J);
		Lwjgl2Keys.map(Key.K, Keyboard.KEY_K);
		Lwjgl2Keys.map(Key.L, Keyboard.KEY_L);
		Lwjgl2Keys.map(Key.M, Keyboard.KEY_M);
		Lwjgl2Keys.map(Key.N, Keyboard.KEY_N);
		Lwjgl2Keys.map(Key.O, Keyboard.KEY_O);
		Lwjgl2Keys.map(Key.P, Keyboard.KEY_P);
		Lwjgl2Keys.map(Key.Q, Keyboard.KEY_Q);
		Lwjgl2Keys.map(Key.R, Keyboard.KEY_R);
		Lwjgl2Keys.map(Key.S, Keyboard.KEY_S);
		Lwjgl2Keys.map(Key.T, Keyboard.KEY_T);
		Lwjgl2Keys.map(Key.U, Keyboard.KEY_U);
		Lwjgl2Keys.map(Key.V, Keyboard.KEY_V);
		Lwjgl2Keys.map(Key.W, Keyboard.KEY_W);
		Lwjgl2Keys.map(Key.X, Keyboard.KEY_X);
		Lwjgl2Keys.map(Key.Y, Keyboard.KEY_Y);
		Lwjgl2Keys.map(Key.Z, Keyboard.KEY_Z);
		Lwjgl2Keys.map(Key.DIGIT_0, Keyboard.KEY_0);
		Lwjgl2Keys.map(Key.DIGIT_1, Keyboard.KEY_1);
		Lwjgl2Keys.map(Key.DIGIT_2, Keyboard.KEY_2);
		Lwjgl2Keys.map(Key.DIGIT_3, Keyboard.KEY_3);
		Lwjgl2Keys.map(Key.DIGIT_4, Keyboard.KEY_4);
		Lwjgl2Keys.map(Key.DIGIT_5, Keyboard.KEY_5);
		Lwjgl2Keys.map(Key.DIGIT_6, Keyboard.KEY_6);
		Lwjgl2Keys.map(Key.DIGIT_7, Keyboard.KEY_7);
		Lwjgl2Keys.map(Key.DIGIT_8, Keyboard.KEY_8);
		Lwjgl2Keys.map(Key.DIGIT_9, Keyboard.KEY_9);
		Lwjgl2Keys.map(Key.F1, Keyboard.KEY_F1);
		Lwjgl2Keys.map(Key.F2, Keyboard.KEY_F2);
		Lwjgl2Keys.map(Key.F3, Keyboard.KEY_F3);
		Lwjgl2Keys.map(Key.F4, Keyboard.KEY_F4);
		Lwjgl2Keys.map(Key.F5, Keyboard.KEY_F5);
		Lwjgl2Keys.map(Key.F6, Keyboard.KEY_F6);
		Lwjgl2Keys.map(Key.F7, Keyboard.KEY_F7);
		Lwjgl2Keys.map(Key.F8, Keyboard.KEY_F8);
		Lwjgl2Keys.map(Key.F9, Keyboard.KEY_F9);
		Lwjgl2Keys.map(Key.F10, Keyboard.KEY_F10);
		Lwjgl2Keys.map(Key.F11, Keyboard.KEY_F11);
		Lwjgl2Keys.map(Key.F12, Keyboard.KEY_F12);
		Lwjgl2Keys.map(Key.F13, Keyboard.KEY_F13);
		Lwjgl2Keys.map(Key.F14, Keyboard.KEY_F14);
		Lwjgl2Keys.map(Key.F15, Keyboard.KEY_F15);
		Lwjgl2Keys.map(Key.F16, Keyboard.KEY_F16);
		Lwjgl2Keys.map(Key.F17, Keyboard.KEY_F17);
		Lwjgl2Keys.map(Key.F18, Keyboard.KEY_F18);
		Lwjgl2Keys.map(Key.F19, Keyboard.KEY_F19);
		Lwjgl2Keys.map(Key.ESCAPE, Keyboard.KEY_ESCAPE);
		Lwjgl2Keys.map(Key.ENTER, Keyboard.KEY_RETURN);
		Lwjgl2Keys.map(Key.TAB, Keyboard.KEY_TAB);
		Lwjgl2Keys.map(Key.BACKSPACE, Keyboard.KEY_BACK);
		Lwjgl2Keys.map(Key.INSERT, Keyboard.KEY_INSERT);
		Lwjgl2Keys.map(Key.DELETE, Keyboard.KEY_DELETE);
		Lwjgl2Keys.map(Key.RIGHT, Keyboard.KEY_RIGHT);
		Lwjgl2Keys.map(Key.LEFT, Keyboard.KEY_LEFT);
		Lwjgl2Keys.map(Key.DOWN, Keyboard.KEY_DOWN);
		Lwjgl2Keys.map(Key.UP, Keyboard.KEY_UP);
		Lwjgl2Keys.map(Key.PAGE_UP, Keyboard.KEY_PRIOR);
		Lwjgl2Keys.map(Key.PAGE_DOWN, Keyboard.KEY_NEXT);
		Lwjgl2Keys.map(Key.HOME, Keyboard.KEY_HOME);
		Lwjgl2Keys.map(Key.END, Keyboard.KEY_END);
		Lwjgl2Keys.map(Key.CAPS_LOCK, Keyboard.KEY_CAPITAL);
		Lwjgl2Keys.map(Key.SCROLL_LOCK, Keyboard.KEY_SCROLL);
		Lwjgl2Keys.map(Key.NUM_LOCK, Keyboard.KEY_NUMLOCK);
		Lwjgl2Keys.map(Key.PRINT_SCREEN, Keyboard.KEY_SYSRQ);
		Lwjgl2Keys.map(Key.PAUSE, Keyboard.KEY_PAUSE);
		Lwjgl2Keys.map(Key.SPACE, Keyboard.KEY_SPACE);
		Lwjgl2Keys.map(Key.APOSTROPHE, Keyboard.KEY_APOSTROPHE);
		Lwjgl2Keys.map(Key.COMMA, Keyboard.KEY_COMMA);
		Lwjgl2Keys.map(Key.MINUS, Keyboard.KEY_MINUS);
		Lwjgl2Keys.map(Key.PERIOD, Keyboard.KEY_PERIOD);
		Lwjgl2Keys.map(Key.SLASH, Keyboard.KEY_SLASH);
		Lwjgl2Keys.map(Key.SEMICOLON, Keyboard.KEY_SEMICOLON);
		Lwjgl2Keys.map(Key.EQUAL, Keyboard.KEY_EQUALS);
		Lwjgl2Keys.map(Key.LEFT_BRACKET, Keyboard.KEY_LBRACKET);
		Lwjgl2Keys.map(Key.BACKSLASH, Keyboard.KEY_BACKSLASH);
		Lwjgl2Keys.map(Key.RIGHT_BRACKET, Keyboard.KEY_RBRACKET);
		Lwjgl2Keys.map(Key.GRAVE_ACCENT, Keyboard.KEY_GRAVE);
		Lwjgl2Keys.map(Key.NUMPAD_0, Keyboard.KEY_NUMPAD0);
		Lwjgl2Keys.map(Key.NUMPAD_1, Keyboard.KEY_NUMPAD1);
		Lwjgl2Keys.map(Key.NUMPAD_2, Keyboard.KEY_NUMPAD2);
		Lwjgl2Keys.map(Key.NUMPAD_3, Keyboard.KEY_NUMPAD3);
		Lwjgl2Keys.map(Key.NUMPAD_4, Keyboard.KEY_NUMPAD4);
		Lwjgl2Keys.map(Key.NUMPAD_5, Keyboard.KEY_NUMPAD5);
		Lwjgl2Keys.map(Key.NUMPAD_6, Keyboard.KEY_NUMPAD6);
		Lwjgl2Keys.map(Key.NUMPAD_7, Keyboard.KEY_NUMPAD7);
		Lwjgl2Keys.map(Key.NUMPAD_8, Keyboard.KEY_NUMPAD8);
		Lwjgl2Keys.map(Key.NUMPAD_9, Keyboard.KEY_NUMPAD9);
		Lwjgl2Keys.map(Key.NUMPAD_DECIMAL, Keyboard.KEY_DECIMAL);
		Lwjgl2Keys.map(Key.NUMPAD_DIVIDE, Keyboard.KEY_DIVIDE);
		Lwjgl2Keys.map(Key.NUMPAD_MULTIPLY, Keyboard.KEY_MULTIPLY);
		Lwjgl2Keys.map(Key.NUMPAD_SUBTRACT, Keyboard.KEY_SUBTRACT);
		Lwjgl2Keys.map(Key.NUMPAD_ADD, Keyboard.KEY_ADD);
		Lwjgl2Keys.map(Key.NUMPAD_ENTER, Keyboard.KEY_NUMPADENTER);
		Lwjgl2Keys.map(Key.NUMPAD_EQUAL, Keyboard.KEY_NUMPADEQUALS);
		Lwjgl2Keys.map(Key.LEFT_SHIFT, Keyboard.KEY_LSHIFT);
		Lwjgl2Keys.map(Key.LEFT_CONTROL, Keyboard.KEY_LCONTROL);
		Lwjgl2Keys.map(Key.LEFT_ALT, Keyboard.KEY_LMENU);
		Lwjgl2Keys.map(Key.LEFT_SUPER, Keyboard.KEY_LMETA);
		Lwjgl2Keys.map(Key.RIGHT_SHIFT, Keyboard.KEY_RSHIFT);
		Lwjgl2Keys.map(Key.RIGHT_CONTROL, Keyboard.KEY_RCONTROL);
		Lwjgl2Keys.map(Key.RIGHT_ALT, Keyboard.KEY_RMENU);
		Lwjgl2Keys.map(Key.RIGHT_SUPER, Keyboard.KEY_RMETA);
		Lwjgl2Keys.map(Key.MENU, Keyboard.KEY_APPS);
	}

	public static @NonNull Key getKey(final int code, final char character) {
		final Key position = Lwjgl2Keys.getPhysicalKey(code);
		if (!KeyLayout.isLayoutKey(position)) {
			return position;
		}

		if (character >= 0x01 && character <= 0x1A) {
			return Key.values()[Key.A.ordinal() + character - 0x01];
		}

		final Key key = KeyLayout.fromCharacter(character);
		return key == null ? position : key;
	}

	public static @NonNull Key getPhysicalKey(final int code) {
		return code < 0 || code >= Lwjgl2Keys.KEY_ARRAY.length ? Key.UNKNOWN : Lwjgl2Keys.KEY_ARRAY[code];
	}

	public static boolean isKeyDown(final @NonNull Key key) {
		final Integer code = Lwjgl2Keys.CODE_MAP.get(key);
		return code != null && Keyboard.isKeyDown(code);
	}

	private static void map(final Key key, final int code) {
		Lwjgl2Keys.CODE_MAP.put(key, code);
		Lwjgl2Keys.KEY_ARRAY[code] = key;
	}

}