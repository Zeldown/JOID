package dev.joid.backend.lwjgl2.window.cursor;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import dev.joid.lib.input.cursor.Cursor;
import lombok.NonNull;

public final class MacNativeCursor extends NativeCursor {

	private final Pointer  cursorClass;
	private final Function messageSend;
	private final Function classMethod;
	private final Function registerName;

	private MacNativeCursor() {
		final NativeLibrary objc = NativeLibrary.getInstance("objc");
		this.messageSend  = objc.getFunction("objc_msgSend");
		this.classMethod  = objc.getFunction("class_getClassMethod");
		this.registerName = objc.getFunction("sel_registerName");
		this.cursorClass  = objc.getFunction("objc_getClass").invokePointer(new Object[] {"NSCursor"});
	}

	public static @NonNull MacNativeCursor create() {
		return new MacNativeCursor();
	}

	@Override
	protected Object load(final @NonNull Cursor cursor) {
		for (final String name : MacNativeCursor.getSelectors(cursor)) {
			final Pointer selector = this.registerName.invokePointer(new Object[] {name});
			if (this.classMethod.invokePointer(new Object[] {this.cursorClass, selector}) != null) {
				final Pointer handle = this.messageSend.invokePointer(new Object[] {this.cursorClass, selector});
				if (handle != null) {
					return Pointer.nativeValue(handle);
				}
			}
		}
		return null;
	}

	private static String[] getSelectors(final Cursor cursor) {
		switch (cursor) {
		case POINTER:
			return new String[] {"pointingHandCursor"};
		case TEXT:
			return new String[] {"IBeamCursor"};
		case CROSSHAIR:
			return new String[] {"crosshairCursor"};
		case MOVE:
			return new String[] {"closedHandCursor"};
		case NOT_ALLOWED:
			return new String[] {"operationNotAllowedCursor"};
		case RESIZE_EW:
			return new String[] {"_windowResizeEastWestCursor", "resizeLeftRightCursor"};
		case RESIZE_NS:
			return new String[] {"_windowResizeNorthSouthCursor", "resizeUpDownCursor"};
		case RESIZE_NWSE:
			return new String[] {"_windowResizeNorthWestSouthEastCursor"};
		case RESIZE_NESW:
			return new String[] {"_windowResizeNorthEastSouthWestCursor"};
		default:
			return new String[] {"arrowCursor"};
		}
	}

}