package dev.joid.base.glfw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import dev.joid.lib.utils.cursor.Cursor;

public class WindowBridgeTest {

	private static long window;

	@BeforeClass
	public static void createHiddenWindow() {
		Assume.assumeTrue(GLFW.glfwInit());
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		WindowBridgeTest.window = GLFW.glfwCreateWindow(64, 64, "JOID cursor test", 0L, 0L);
		Assume.assumeTrue(WindowBridgeTest.window != 0L);
	}

	@AfterClass
	public static void destroyHiddenWindow() {
		if (WindowBridgeTest.window != 0L) {
			GLFW.glfwDestroyWindow(WindowBridgeTest.window);
		}
		GLFW.glfwTerminate();
	}

	@Test
	public void showsEveryCursorWithoutError() {
		final List<Integer> errors = WindowBridgeTest.record(() -> {
			final WindowBridge bridge = new WindowBridge(WindowBridgeTest.window);
			for (final Cursor cursor : Cursor.values()) {
				bridge.setCursor(cursor);
				Assert.assertSame(cursor, bridge.getCursor());
			}
			bridge.destroy();
			Assert.assertSame(Cursor.DEFAULT, bridge.getCursor());
		});
		errors.removeIf(error -> error == GLFW.GLFW_CURSOR_UNAVAILABLE);
		Assert.assertEquals(Collections.emptyList(), errors);
	}

	@Test
	public void reusesItsCursorsUntilDestroyed() {
		final List<Integer> errors = WindowBridgeTest.record(() -> {
			final WindowBridge bridge = new WindowBridge(WindowBridgeTest.window);
			for (int i = 0; i < 3; i++) {
				bridge.setCursor(Cursor.TEXT);
				bridge.setCursor(Cursor.DEFAULT);
			}
			bridge.destroy();
			bridge.destroy();
			bridge.setCursor(Cursor.POINTER);
			bridge.destroy();
		});
		Assert.assertEquals(Collections.emptyList(), errors);
	}

	private static List<Integer> record(final Runnable runnable) {
		final List<Integer> errors = new ArrayList<>();
		final GLFWErrorCallback recorder = GLFWErrorCallback.create((error, description) -> errors.add(error));
		final GLFWErrorCallback previous = GLFW.glfwSetErrorCallback(recorder);
		try {
			runnable.run();
		} finally {
			GLFW.glfwSetErrorCallback(previous);
			recorder.free();
		}
		return errors;
	}

}