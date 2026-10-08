package dev.joid.base.glfw.input;

import org.lwjgl.glfw.GLFW;

import dev.joid.lib.utils.key.Key;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyCharacterMerger {

	private final IKeyTypedListener listener;

	@Getter
	private Key pendingKey;

	public static @NonNull KeyCharacterMerger create(final @NonNull IKeyTypedListener listener) {
		return new KeyCharacterMerger(listener);
	}

	public void keyPressed(final @NonNull Key key, final int code, final int modifiers) {
		this.flush();
		if (KeyCharacterMerger.isTextKey(code) && (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT)) == 0) {
			this.pendingKey = key;
			return;
		}

		this.listener.keyTyped((char) 0, key);
	}

	public void charTyped(final int codepoint) {
		final Key key = this.pendingKey == null ? Key.UNKNOWN : this.pendingKey;
		this.pendingKey = null;

		final char[] chars = Character.toChars(codepoint);
		this.listener.keyTyped(chars[0], key);
		for (int i = 1; i < chars.length; i++) {
			this.listener.keyTyped(chars[i], Key.UNKNOWN);
		}
	}

	public void flush() {
		if (this.pendingKey == null) {
			return;
		}

		final Key key = this.pendingKey;
		this.pendingKey = null;
		this.listener.keyTyped((char) 0, key);
	}

	private static boolean isTextKey(final int code) {
		return code >= GLFW.GLFW_KEY_SPACE && code <= GLFW.GLFW_KEY_GRAVE_ACCENT || code >= GLFW.GLFW_KEY_KP_0 && code <= GLFW.GLFW_KEY_KP_ADD;
	}

}