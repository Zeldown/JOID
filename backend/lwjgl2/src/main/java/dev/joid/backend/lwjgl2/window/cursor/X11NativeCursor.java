package dev.joid.backend.lwjgl2.window.cursor;

import java.lang.reflect.Method;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import dev.joid.lib.input.cursor.Cursor;
import lombok.NonNull;

public final class X11NativeCursor extends NativeCursor {

	private final Function createFontCursor;
	private final Function libraryLoadCursor;

	private X11NativeCursor() {
		this.createFontCursor  = X11NativeCursor.getLibrary("X11", "libX11.so.6").getFunction("XCreateFontCursor");
		this.libraryLoadCursor = X11NativeCursor.findFunction("Xcursor", "libXcursor.so.1", "XcursorLibraryLoadCursor");
	}

	public static @NonNull X11NativeCursor create() {
		return new X11NativeCursor();
	}

	@Override
	protected Object load(final @NonNull Cursor cursor) throws ReflectiveOperationException {
		final Method getDisplay = Class.forName("org.lwjgl.opengl.LinuxDisplay").getDeclaredMethod("getDisplay");
		getDisplay.setAccessible(true);
		final Pointer display = Pointer.createConstant((Long) getDisplay.invoke(null));

		if (this.libraryLoadCursor != null) {
			for (final String name : X11NativeCursor.getNames(cursor)) {
				final Pointer handle = this.libraryLoadCursor.invokePointer(new Object[] {display, name});
				if (handle != null) {
					return Pointer.nativeValue(handle);
				}
			}
		}

		final int shape = X11NativeCursor.getShape(cursor);
		final Pointer handle = shape < 0 ? null : this.createFontCursor.invokePointer(new Object[] {display, shape});
		return handle == null ? null : Pointer.nativeValue(handle);
	}

	private static NativeLibrary getLibrary(final String name, final String file) {
		try {
			return NativeLibrary.getInstance(name);
		} catch (final UnsatisfiedLinkError error) {
			return NativeLibrary.getInstance(file);
		}
	}

	private static Function findFunction(final String name, final String file, final String function) {
		try {
			return X11NativeCursor.getLibrary(name, file).getFunction(function);
		} catch (final UnsatisfiedLinkError error) {
			NativeCursor.warn("libXcursor cannot be loaded, the X11 cursors come from the cursor font: " + error.getMessage());
			return null;
		}
	}

	private static int getShape(final Cursor cursor) {
		switch (cursor) {
		case POINTER:
			return 60;
		case TEXT:
			return 152;
		case CROSSHAIR:
			return 34;
		case MOVE:
			return 52;
		case RESIZE_EW:
			return 108;
		case RESIZE_NS:
			return 116;
		case DEFAULT:
			return 68;
		default:
			return -1;
		}
	}

	private static String[] getNames(final Cursor cursor) {
		switch (cursor) {
		case POINTER:
			return new String[] {"pointer", "hand2"};
		case TEXT:
			return new String[] {"text", "xterm"};
		case CROSSHAIR:
			return new String[] {"crosshair", "cross"};
		case MOVE:
			return new String[] {"all-scroll", "fleur", "size_all"};
		case NOT_ALLOWED:
			return new String[] {"not-allowed", "crossed_circle"};
		case RESIZE_EW:
			return new String[] {"ew-resize", "sb_h_double_arrow"};
		case RESIZE_NS:
			return new String[] {"ns-resize", "sb_v_double_arrow"};
		case RESIZE_NWSE:
			return new String[] {"nwse-resize", "bd_double_arrow", "size_fdiag"};
		case RESIZE_NESW:
			return new String[] {"nesw-resize", "fd_double_arrow", "size_bdiag"};
		default:
			return new String[] {"default", "left_ptr"};
		}
	}

}