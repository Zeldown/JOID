package dev.joid.base.lwjgl3.sdl.input;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;

import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLScancode;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.KeyLayout;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SdlKeys {

	private static final KeyLayout         LAYOUT   = KeyLayout.from(key -> SDLKeyboard.SDL_GetKeyName(SDLKeyboard.SDL_GetKeyFromScancode(SdlKeys.getCode(key), (short) 0, false)));
	private static final Map<Integer, Key> KEY_MAP  = new HashMap<>();
	private static final Map<Key, Integer> CODE_MAP = new EnumMap<>(Key.class);

	static {
		SdlKeys.map(Key.A, Key.Z, SDLScancode.SDL_SCANCODE_A);
		SdlKeys.map(Key.DIGIT_0, SDLScancode.SDL_SCANCODE_0);
		SdlKeys.map(Key.DIGIT_1, Key.DIGIT_9, SDLScancode.SDL_SCANCODE_1);
		SdlKeys.map(Key.F1, Key.F12, SDLScancode.SDL_SCANCODE_F1);
		SdlKeys.map(Key.F13, Key.F24, SDLScancode.SDL_SCANCODE_F13);
		SdlKeys.map(Key.ESCAPE, SDLScancode.SDL_SCANCODE_ESCAPE);
		SdlKeys.map(Key.ENTER, SDLScancode.SDL_SCANCODE_RETURN);
		SdlKeys.map(Key.TAB, SDLScancode.SDL_SCANCODE_TAB);
		SdlKeys.map(Key.BACKSPACE, SDLScancode.SDL_SCANCODE_BACKSPACE);
		SdlKeys.map(Key.INSERT, SDLScancode.SDL_SCANCODE_INSERT);
		SdlKeys.map(Key.DELETE, SDLScancode.SDL_SCANCODE_DELETE);
		SdlKeys.map(Key.RIGHT, Key.UP, SDLScancode.SDL_SCANCODE_RIGHT);
		SdlKeys.map(Key.PAGE_UP, SDLScancode.SDL_SCANCODE_PAGEUP);
		SdlKeys.map(Key.PAGE_DOWN, SDLScancode.SDL_SCANCODE_PAGEDOWN);
		SdlKeys.map(Key.HOME, SDLScancode.SDL_SCANCODE_HOME);
		SdlKeys.map(Key.END, SDLScancode.SDL_SCANCODE_END);
		SdlKeys.map(Key.CAPS_LOCK, SDLScancode.SDL_SCANCODE_CAPSLOCK);
		SdlKeys.map(Key.SCROLL_LOCK, SDLScancode.SDL_SCANCODE_SCROLLLOCK);
		SdlKeys.map(Key.NUM_LOCK, SDLScancode.SDL_SCANCODE_NUMLOCKCLEAR);
		SdlKeys.map(Key.PRINT_SCREEN, SDLScancode.SDL_SCANCODE_PRINTSCREEN);
		SdlKeys.map(Key.PAUSE, SDLScancode.SDL_SCANCODE_PAUSE);
		SdlKeys.map(Key.SPACE, SDLScancode.SDL_SCANCODE_SPACE);
		SdlKeys.map(Key.APOSTROPHE, SDLScancode.SDL_SCANCODE_APOSTROPHE);
		SdlKeys.map(Key.COMMA, SDLScancode.SDL_SCANCODE_COMMA);
		SdlKeys.map(Key.MINUS, SDLScancode.SDL_SCANCODE_MINUS);
		SdlKeys.map(Key.PERIOD, SDLScancode.SDL_SCANCODE_PERIOD);
		SdlKeys.map(Key.SLASH, SDLScancode.SDL_SCANCODE_SLASH);
		SdlKeys.map(Key.SEMICOLON, SDLScancode.SDL_SCANCODE_SEMICOLON);
		SdlKeys.map(Key.EQUAL, SDLScancode.SDL_SCANCODE_EQUALS);
		SdlKeys.map(Key.LEFT_BRACKET, SDLScancode.SDL_SCANCODE_LEFTBRACKET);
		SdlKeys.map(Key.BACKSLASH, SDLScancode.SDL_SCANCODE_BACKSLASH);
		SdlKeys.map(Key.RIGHT_BRACKET, SDLScancode.SDL_SCANCODE_RIGHTBRACKET);
		SdlKeys.map(Key.GRAVE_ACCENT, SDLScancode.SDL_SCANCODE_GRAVE);
		SdlKeys.map(Key.NUMPAD_0, SDLScancode.SDL_SCANCODE_KP_0);
		SdlKeys.map(Key.NUMPAD_1, Key.NUMPAD_9, SDLScancode.SDL_SCANCODE_KP_1);
		SdlKeys.map(Key.NUMPAD_DECIMAL, SDLScancode.SDL_SCANCODE_KP_PERIOD);
		SdlKeys.map(Key.NUMPAD_DIVIDE, SDLScancode.SDL_SCANCODE_KP_DIVIDE);
		SdlKeys.map(Key.NUMPAD_MULTIPLY, SDLScancode.SDL_SCANCODE_KP_MULTIPLY);
		SdlKeys.map(Key.NUMPAD_SUBTRACT, SDLScancode.SDL_SCANCODE_KP_MINUS);
		SdlKeys.map(Key.NUMPAD_ADD, SDLScancode.SDL_SCANCODE_KP_PLUS);
		SdlKeys.map(Key.NUMPAD_ENTER, SDLScancode.SDL_SCANCODE_KP_ENTER);
		SdlKeys.map(Key.NUMPAD_EQUAL, SDLScancode.SDL_SCANCODE_KP_EQUALS);
		SdlKeys.map(Key.LEFT_SHIFT, SDLScancode.SDL_SCANCODE_LSHIFT);
		SdlKeys.map(Key.LEFT_CONTROL, SDLScancode.SDL_SCANCODE_LCTRL);
		SdlKeys.map(Key.LEFT_ALT, SDLScancode.SDL_SCANCODE_LALT);
		SdlKeys.map(Key.LEFT_SUPER, SDLScancode.SDL_SCANCODE_LGUI);
		SdlKeys.map(Key.RIGHT_SHIFT, SDLScancode.SDL_SCANCODE_RSHIFT);
		SdlKeys.map(Key.RIGHT_CONTROL, SDLScancode.SDL_SCANCODE_RCTRL);
		SdlKeys.map(Key.RIGHT_ALT, SDLScancode.SDL_SCANCODE_RALT);
		SdlKeys.map(Key.RIGHT_SUPER, SDLScancode.SDL_SCANCODE_RGUI);
		SdlKeys.map(Key.MENU, SDLScancode.SDL_SCANCODE_APPLICATION);
	}

	public static int getCode(final @NonNull Key key) {
		return SdlKeys.CODE_MAP.getOrDefault(key, SDLScancode.SDL_SCANCODE_UNKNOWN);
	}

	public static @NonNull Key getKey(final int code) {
		return SdlKeys.LAYOUT.translate(SdlKeys.getPhysicalKey(code));
	}

	public static @NonNull Key getPhysicalKey(final int code) {
		return SdlKeys.KEY_MAP.getOrDefault(code, Key.UNKNOWN);
	}

	public static boolean isKeyDown(final @NonNull Key key, final @NonNull IntPredicate down) {
		final int code = SdlKeys.getCode(key);
		return code != SDLScancode.SDL_SCANCODE_UNKNOWN && down.test(code);
	}

	private static void map(final Key key, final int code) {
		SdlKeys.CODE_MAP.put(key, code);
		SdlKeys.KEY_MAP.put(code, key);
	}

	private static void map(final Key first, final Key last, final int code) {
		for (final Key key : EnumSet.range(first, last)) {
			SdlKeys.map(key, code + key.ordinal() - first.ordinal());
		}
	}

}