package dev.joid.backend.lwjgl2.window;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import dev.joid.backend.lwjgl2.input.Lwjgl2Keys;
import dev.joid.backend.lwjgl2.window.cursor.NativeCursor;
import dev.joid.backend.lwjgl2.window.cursor.NativeCursors;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2WindowBridge implements IWindowBridge {

	public static @NonNull Lwjgl2WindowBridge create() {
		return new Lwjgl2WindowBridge();
	}

	@Override
	public int getWidth() {
		return Math.round(Display.getWidth() * Display.getPixelScaleFactor());
	}

	@Override
	public int getHeight() {
		return Math.round(Display.getHeight() * Display.getPixelScaleFactor());
	}

	@Override
	public double getMouseX() {
		return Mouse.getX() * (double) Display.getPixelScaleFactor();
	}

	@Override
	public double getMouseY() {
		return (Display.getHeight() - 1 - Mouse.getY()) * (double) Display.getPixelScaleFactor();
	}

	@Override
	public @NonNull String getClipboard() {
		try {
			final Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (final Exception exception) {
			JOIDLog.devWarnOnce("Lwjgl2WindowBridge.getClipboard", () -> "The clipboard cannot be read, an empty text replaces it: " + exception);
		}

		return "";
	}

	@Override
	public boolean isMouseGrabbed() {
		return Mouse.isGrabbed();
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		return Lwjgl2Keys.isKeyDown(key);
	}

	@Override
	public void setCursor(final @NonNull Cursor cursor) {
		final NativeCursor nativeCursor = NativeCursors.get();
		if (nativeCursor != null && !Mouse.isGrabbed()) {
			nativeCursor.apply(cursor);
		}
	}

	@Override
	public void setClipboard(final @NonNull String text) {
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
		} catch (final Exception exception) {
			JOIDLog.devWarnOnce("Lwjgl2WindowBridge.setClipboard", () -> "The clipboard cannot be written, the text stays out of it: " + exception);
		}
	}

}