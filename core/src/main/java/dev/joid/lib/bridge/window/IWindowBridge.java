package dev.joid.lib.bridge.window;

import dev.joid.lib.bridge.IBridge;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import lombok.NonNull;

public interface IWindowBridge extends IBridge {

	public int getWidth();
	public int getHeight();

	public double getMouseX();
	public double getMouseY();
	public boolean isMouseGrabbed();
	public boolean isKeyDown(final @NonNull Key physicalKey);

	public @NonNull String getClipboard();
	public void setClipboard(final @NonNull String text);

	public default void setCursor(final @NonNull Cursor cursor) {}

}