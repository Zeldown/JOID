package dev.joid.backend.lwjgl2.window.cursor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import dev.joid.lib.input.cursor.Cursor;
import lombok.NonNull;

public final class WindowsNativeCursor extends NativeCursor {

	private final Function loadCursor;

	private WindowsNativeCursor() throws ReflectiveOperationException {
		this.loadCursor = Function.getFunction("user32", "LoadCursorW", Function.class.getField("ALT_CONVENTION").getInt(null));
	}

	public static @NonNull WindowsNativeCursor create() throws ReflectiveOperationException {
		return new WindowsNativeCursor();
	}

	@Override
	protected Object load(final @NonNull Cursor cursor) {
		final Pointer pointer = this.loadCursor.invokePointer(new Object[] {null, Pointer.createConstant(WindowsNativeCursor.getId(cursor))});
		if (pointer == null) {
			return null;
		}

		final ByteBuffer handle = ByteBuffer.allocateDirect(Native.POINTER_SIZE).order(ByteOrder.nativeOrder());
		if (Native.POINTER_SIZE == 8) {
			handle.putLong(0, Pointer.nativeValue(pointer));
		} else {
			handle.putInt(0, (int) Pointer.nativeValue(pointer));
		}
		return handle;
	}

	private static int getId(final Cursor cursor) {
		switch (cursor) {
		case POINTER:
			return 32649;
		case TEXT:
			return 32513;
		case CROSSHAIR:
			return 32515;
		case MOVE:
			return 32646;
		case NOT_ALLOWED:
			return 32648;
		case RESIZE_EW:
			return 32644;
		case RESIZE_NS:
			return 32645;
		case RESIZE_NWSE:
			return 32642;
		case RESIZE_NESW:
			return 32643;
		default:
			return 32512;
		}
	}

}