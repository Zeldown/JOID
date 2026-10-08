package dev.joid.base.glfw;

import org.junit.Assert;
import org.junit.Test;

public class GlfwWindowsTest {

	@Test
	public void scalesACursorPositionToTheFramebuffer() {
		Assert.assertEquals(300D, GlfwWindows.toFramebuffer(150D, 800, 1600), 0D);
		Assert.assertEquals(75D, GlfwWindows.toFramebuffer(150D, 1600, 800), 0D);
	}

	@Test
	public void keepsTheCursorOfAnEmptyWindow() {
		Assert.assertEquals(150D, GlfwWindows.toFramebuffer(150D, 0, 1600), 0D);
	}

}