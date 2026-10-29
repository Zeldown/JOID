package dev.joid.base.lwjgl2.window;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.input.Mouse;

public class Lwjgl2WindowBridgeTest {

	@Test
	public void reportsTheMouseOutsideTheWindow() {
		final boolean clip = Mouse.isClipMouseCoordinatesToWindow();
		try {
			Mouse.setClipMouseCoordinatesToWindow(true);
			new Lwjgl2WindowBridge();
			Assert.assertFalse(Mouse.isClipMouseCoordinatesToWindow());
		} finally {
			Mouse.setClipMouseCoordinatesToWindow(clip);
		}
	}

}