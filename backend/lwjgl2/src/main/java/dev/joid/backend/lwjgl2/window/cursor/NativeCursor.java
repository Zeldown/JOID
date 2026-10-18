package dev.joid.backend.lwjgl2.window.cursor;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.Display;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.utils.platform.Platform;

public abstract class NativeCursor {

	private final Map<Cursor, Object> handleMap;

	private Cursor  applied;
	private boolean failed;
	private Object  implementation;
	private Method  setNativeCursor;

	protected NativeCursor() {
		this.handleMap = new EnumMap<>(Cursor.class);
		this.applied   = Cursor.DEFAULT;
	}

	public static NativeCursor forPlatform() {
		try {
			Class.forName("com.sun.jna.Native");
			switch (Platform.current()) {
			case WINDOWS:
				return new WindowsNativeCursor();
			case MACOS:
				return new MacNativeCursor();
			default:
				return new X11NativeCursor();
			}
		} catch (final ReflectiveOperationException | LinkageError | RuntimeException exception) {
			JOIDLog.devWarnOnce("NativeCursor.jna", () -> "JNA is missing or cannot be loaded, the mouse cursor stays the default one: " + exception);
			return null;
		}
	}

	public final void apply(final Cursor cursor) {
		if (cursor == this.applied || this.failed || !Display.isCreated()) {
			return;
		}

		try {
			if (this.setNativeCursor == null) {
				final Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
				getImplementation.setAccessible(true);
				this.implementation = getImplementation.invoke(null);
				this.setNativeCursor = Class.forName("org.lwjgl.opengl.InputImplementation").getDeclaredMethod("setNativeCursor", Object.class);
				this.setNativeCursor.setAccessible(true);
			}

			this.setNativeCursor.invoke(this.implementation, this.getHandle(cursor));
			this.applied = cursor;
		} catch (final ReflectiveOperationException | LinkageError | RuntimeException exception) {
			this.failed = true;
			JOIDLog.devWarnOnce("NativeCursor.apply", () -> "The native cursors of LWJGL 2 cannot be set, the mouse cursor stays the default one: " + exception);
		}
	}

	protected abstract Object load(final Cursor cursor) throws ReflectiveOperationException;

	private Object getHandle(final Cursor cursor) throws ReflectiveOperationException {
		if (cursor == Cursor.DEFAULT) {
			return null;
		}

		if (!this.handleMap.containsKey(cursor)) {
			Object handle = null;
			try {
				handle = this.load(cursor);
			} catch (final LinkageError | RuntimeException exception) {
				JOIDLog.devWarnOnce("NativeCursor." + cursor, () -> "The " + cursor + " cursor cannot be loaded (" + exception + "), the default one replaces it: check that the system has this cursor");
			}
			this.handleMap.put(cursor, handle);
		}
		return this.handleMap.get(cursor);
	}

}