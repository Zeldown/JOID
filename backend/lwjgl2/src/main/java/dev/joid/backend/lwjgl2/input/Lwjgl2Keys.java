package dev.joid.backend.lwjgl2.input;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.KeyLayout;
import dev.joid.lib.ui.core.UI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2Keys {

	private static final Key[]             KEY_ARRAY  = new Key[Keyboard.KEYBOARD_SIZE];
	private static final Map<Key, Key>     LAYOUT_MAP = new EnumMap<>(Key.class);
	private static final Map<Key, Integer> CODE_MAP   = new EnumMap<>(Key.class);

	static {
		Arrays.fill(Lwjgl2Keys.KEY_ARRAY, Key.UNKNOWN);
		for (final Key key : Key.values()) {
			final int code = Keyboard.getKeyIndex(key.name().replace("DIGIT_", "").replace("NUMPAD_", "NUMPAD"));
			if (code != Keyboard.KEY_NONE) {
				Lwjgl2Keys.map(key, code);
			}
		}

		Lwjgl2Keys.map(Key.ENTER, Keyboard.KEY_RETURN);
		Lwjgl2Keys.map(Key.BACKSPACE, Keyboard.KEY_BACK);
		Lwjgl2Keys.map(Key.PAGE_UP, Keyboard.KEY_PRIOR);
		Lwjgl2Keys.map(Key.PAGE_DOWN, Keyboard.KEY_NEXT);
		Lwjgl2Keys.map(Key.CAPS_LOCK, Keyboard.KEY_CAPITAL);
		Lwjgl2Keys.map(Key.SCROLL_LOCK, Keyboard.KEY_SCROLL);
		Lwjgl2Keys.map(Key.NUM_LOCK, Keyboard.KEY_NUMLOCK);
		Lwjgl2Keys.map(Key.PRINT_SCREEN, Keyboard.KEY_SYSRQ);
		Lwjgl2Keys.map(Key.EQUAL, Keyboard.KEY_EQUALS);
		Lwjgl2Keys.map(Key.LEFT_BRACKET, Keyboard.KEY_LBRACKET);
		Lwjgl2Keys.map(Key.RIGHT_BRACKET, Keyboard.KEY_RBRACKET);
		Lwjgl2Keys.map(Key.GRAVE_ACCENT, Keyboard.KEY_GRAVE);
		Lwjgl2Keys.map(Key.NUMPAD_DECIMAL, Keyboard.KEY_DECIMAL);
		Lwjgl2Keys.map(Key.NUMPAD_DIVIDE, Keyboard.KEY_DIVIDE);
		Lwjgl2Keys.map(Key.NUMPAD_MULTIPLY, Keyboard.KEY_MULTIPLY);
		Lwjgl2Keys.map(Key.NUMPAD_SUBTRACT, Keyboard.KEY_SUBTRACT);
		Lwjgl2Keys.map(Key.NUMPAD_ADD, Keyboard.KEY_ADD);
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

		if (UI.isShiftKeyDown() || UI.isCtrlKeyDown() || UI.isAltKeyDown()) {
			return Lwjgl2Keys.LAYOUT_MAP.getOrDefault(position, position);
		}

		final Key key = KeyLayout.fromCharacter(character);
		if (key == null) {
			return position;
		}

		Lwjgl2Keys.LAYOUT_MAP.put(position, key);
		return key;
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