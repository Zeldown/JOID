package dev.joid.base.glfw.input;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;

import org.lwjgl.glfw.GLFW;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.KeyLayout;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwKeys {

	private static final KeyLayout         LAYOUT   = KeyLayout.from(key -> GLFW.glfwGetKeyName(GlfwKeys.getCode(key), 0));
	private static final Map<Integer, Key> KEY_MAP  = new HashMap<>();
	private static final Map<Key, Integer> CODE_MAP = new EnumMap<>(Key.class);

	static {
		GlfwKeys.map(Key.A, Key.Z, GLFW.GLFW_KEY_A);
		GlfwKeys.map(Key.DIGIT_0, Key.DIGIT_9, GLFW.GLFW_KEY_0);
		GlfwKeys.map(Key.F1, Key.F25, GLFW.GLFW_KEY_F1);
		GlfwKeys.map(Key.ESCAPE, Key.END, GLFW.GLFW_KEY_ESCAPE);
		GlfwKeys.map(Key.CAPS_LOCK, Key.PAUSE, GLFW.GLFW_KEY_CAPS_LOCK);
		GlfwKeys.map(Key.SPACE, GLFW.GLFW_KEY_SPACE);
		GlfwKeys.map(Key.APOSTROPHE, GLFW.GLFW_KEY_APOSTROPHE);
		GlfwKeys.map(Key.COMMA, Key.SLASH, GLFW.GLFW_KEY_COMMA);
		GlfwKeys.map(Key.SEMICOLON, GLFW.GLFW_KEY_SEMICOLON);
		GlfwKeys.map(Key.EQUAL, GLFW.GLFW_KEY_EQUAL);
		GlfwKeys.map(Key.LEFT_BRACKET, Key.RIGHT_BRACKET, GLFW.GLFW_KEY_LEFT_BRACKET);
		GlfwKeys.map(Key.GRAVE_ACCENT, GLFW.GLFW_KEY_GRAVE_ACCENT);
		GlfwKeys.map(Key.NUMPAD_0, Key.NUMPAD_EQUAL, GLFW.GLFW_KEY_KP_0);
		GlfwKeys.map(Key.LEFT_SHIFT, Key.MENU, GLFW.GLFW_KEY_LEFT_SHIFT);
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
		final int code = GlfwKeys.getCode(key);
		return code != GLFW.GLFW_KEY_UNKNOWN && down.test(code);
	}

	private static void map(final Key key, final int code) {
		GlfwKeys.CODE_MAP.put(key, code);
		GlfwKeys.KEY_MAP.put(code, key);
	}

	private static void map(final Key first, final Key last, final int code) {
		for (final Key key : EnumSet.range(first, last)) {
			GlfwKeys.map(key, code + key.ordinal() - first.ordinal());
		}
	}

}