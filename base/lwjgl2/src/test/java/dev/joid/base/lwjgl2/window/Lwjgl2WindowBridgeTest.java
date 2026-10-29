package dev.joid.base.lwjgl2.window;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.input.Mouse;

import dev.joid.lib.input.cursor.Cursor;

public class Lwjgl2WindowBridgeTest {

	@Test
	public void leavesTheClippingOfTheMouseAsTheHostSetIt() {
		final boolean clip = Mouse.isClipMouseCoordinatesToWindow();
		try {
			Mouse.setClipMouseCoordinatesToWindow(true);
			new Lwjgl2WindowBridge();
			Assert.assertTrue(Mouse.isClipMouseCoordinatesToWindow());
		} finally {
			Mouse.setClipMouseCoordinatesToWindow(clip);
		}
	}

	@Test
	public void loadsNoNativeCursorUntilACursorIsSet() throws ReflectiveOperationException {
		final Lwjgl2WindowBridge bridge = new Lwjgl2WindowBridge();
		bridge.setCursor(Cursor.DEFAULT);
		final Field loaded = Lwjgl2WindowBridge.class.getDeclaredField("nativeCursorLoaded");
		loaded.setAccessible(true);
		Assert.assertFalse(loaded.getBoolean(bridge));
	}

}