package dev.joid.lib.input.key;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyLayout {

	private static final Set<Key>            LAYOUT_KEYS   = EnumSet.range(Key.A, Key.Z);
	private static final Map<Character, Key> CHARACTER_MAP = new HashMap<>();

	static {
		KeyLayout.LAYOUT_KEYS.addAll(EnumSet.range(Key.APOSTROPHE, Key.GRAVE_ACCENT));
		for (final Key key : EnumSet.range(Key.A, Key.Z)) {
			KeyLayout.CHARACTER_MAP.put(Character.toLowerCase(key.name().charAt(0)), key);
		}

		KeyLayout.CHARACTER_MAP.put(',', Key.COMMA);
		KeyLayout.CHARACTER_MAP.put('-', Key.MINUS);
		KeyLayout.CHARACTER_MAP.put('/', Key.SLASH);
		KeyLayout.CHARACTER_MAP.put('=', Key.EQUAL);
		KeyLayout.CHARACTER_MAP.put('.', Key.PERIOD);
		KeyLayout.CHARACTER_MAP.put(';', Key.SEMICOLON);
		KeyLayout.CHARACTER_MAP.put('\\', Key.BACKSLASH);
		KeyLayout.CHARACTER_MAP.put('\'', Key.APOSTROPHE);
		KeyLayout.CHARACTER_MAP.put('`', Key.GRAVE_ACCENT);
		KeyLayout.CHARACTER_MAP.put('[', Key.LEFT_BRACKET);
		KeyLayout.CHARACTER_MAP.put(']', Key.RIGHT_BRACKET);
	}

	private final Function<@NonNull Key, String> nameFunction;

	public static @NonNull KeyLayout create(final @NonNull Function<@NonNull Key, String> nameFunction) {
		return new KeyLayout(nameFunction);
	}

	public @NonNull Key translate(final @NonNull Key key) {
		if (!KeyLayout.isLayoutKey(key)) {
			return key;
		}

		final String name = this.nameFunction.apply(key);
		if (name == null || name.length() != 1) {
			return key;
		}

		return KeyLayout.CHARACTER_MAP.getOrDefault(Character.toLowerCase(name.charAt(0)), key);
	}

	public static boolean isLayoutKey(final @NonNull Key key) {
		return KeyLayout.LAYOUT_KEYS.contains(key);
	}

}