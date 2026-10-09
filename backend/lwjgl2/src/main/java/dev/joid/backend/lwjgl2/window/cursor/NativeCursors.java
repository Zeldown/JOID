package dev.joid.backend.lwjgl2.window.cursor;

import dev.joid.lib.utils.platform.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NativeCursors {

	private static final NativeCursor INSTANCE = NativeCursors.create();

	public static NativeCursor get() {
		return NativeCursors.INSTANCE;
	}

	private static NativeCursor create() {
		try {
			Class.forName("com.sun.jna.Native");
			switch (Platform.current()) {
			case WINDOWS:
				return WindowsNativeCursor.create();
			case MACOS:
				return MacNativeCursor.create();
			default:
				return X11NativeCursor.create();
			}
		} catch (final ReflectiveOperationException | LinkageError | RuntimeException exception) {
			NativeCursor.warn("JNA is missing or cannot be loaded, the mouse cursor stays the default one: " + exception);
			return null;
		}
	}

}