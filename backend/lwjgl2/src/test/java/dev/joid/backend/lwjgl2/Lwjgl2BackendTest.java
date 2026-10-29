package dev.joid.backend.lwjgl2;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.input.Mouse;

public class Lwjgl2BackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), Lwjgl2Backend.create().getVersion());
	}

	@Test
	public void reportsTheMouseOutsideTheWindowOnceTheInputIsAttached() {
		final boolean clip = Mouse.isClipMouseCoordinatesToWindow();
		try {
			Mouse.setClipMouseCoordinatesToWindow(true);
			Lwjgl2Backend.create().attachInput();
			Assert.assertFalse(Mouse.isClipMouseCoordinatesToWindow());
		} finally {
			Mouse.setClipMouseCoordinatesToWindow(clip);
		}
	}

}