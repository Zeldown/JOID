package dev.joid.backend.lwjgl2.window.cursor;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.Display;

import dev.joid.internal.JOID;
import dev.joid.lib.input.cursor.Cursor;
import lombok.NonNull;

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

	public final void apply(final @NonNull Cursor cursor) {
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
			NativeCursor.warn("The native cursors of LWJGL 2 cannot be set, the mouse cursor stays the default one: " + exception);
		}
	}

	protected abstract Object load(final @NonNull Cursor cursor) throws ReflectiveOperationException;

	private Object getHandle(final Cursor cursor) throws ReflectiveOperationException {
		if (cursor == Cursor.DEFAULT) {
			return null;
		}

		if (!this.handleMap.containsKey(cursor)) {
			Object handle = null;
			try {
				handle = this.load(cursor);
			} catch (final LinkageError | RuntimeException exception) {
				NativeCursor.warn("The " + cursor + " cursor cannot be loaded, the default one replaces it: " + exception);
			}
			this.handleMap.put(cursor, handle);
		}
		return this.handleMap.get(cursor);
	}

	protected static void warn(final @NonNull String message) {
		if (JOID.inst().isDevMode()) {
			System.err.println("[JOID] " + message);
		}
	}

}