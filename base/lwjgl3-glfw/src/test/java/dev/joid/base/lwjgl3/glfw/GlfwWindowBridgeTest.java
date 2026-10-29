package dev.joid.base.lwjgl3.glfw;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import dev.joid.lib.input.cursor.Cursor;

public class GlfwWindowBridgeTest {

	private static long window;

	@BeforeClass
	public static void createHiddenWindow() {
		Assume.assumeTrue(GLFW.glfwInit());
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GlfwWindowBridgeTest.window = GLFW.glfwCreateWindow(64, 64, "JOID cursor test", 0L, 0L);
		Assume.assumeTrue(GlfwWindowBridgeTest.window != 0L);
	}

	@AfterClass
	public static void destroyHiddenWindow() {
		if (GlfwWindowBridgeTest.window != 0L) {
			GLFW.glfwDestroyWindow(GlfwWindowBridgeTest.window);
		}
		GLFW.glfwTerminate();
	}

	@Test
	public void showsEveryCursorWithoutError() throws Exception {
		final GlfwWindowBridge bridge = new GlfwWindowBridge(GlfwWindowBridgeTest.window);
		final Map<?, ?> cursorMap = GlfwWindowBridgeTest.cursorMap(bridge);
		final List<Integer> errors = GlfwWindowBridgeTest.record(() -> {
			for (final Cursor cursor : Cursor.values()) {
				bridge.setCursor(cursor);
				Assert.assertEquals(cursor != Cursor.DEFAULT, cursorMap.containsKey(cursor));
			}
			bridge.destroy();
			Assert.assertTrue(cursorMap.isEmpty());
		});
		errors.removeIf(error -> error == GLFW.GLFW_CURSOR_UNAVAILABLE);
		Assert.assertEquals(Collections.emptyList(), errors);
	}

	@Test
	public void reusesItsCursorsUntilDestroyed() {
		final List<Integer> errors = GlfwWindowBridgeTest.record(() -> {
			final GlfwWindowBridge bridge = new GlfwWindowBridge(GlfwWindowBridgeTest.window);
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

	@Test
	public void asksGlfw33OnlyForTheShapesItHas() throws Exception {
		for (final Cursor cursor : new Cursor[] {Cursor.MOVE, Cursor.NOT_ALLOWED, Cursor.RESIZE_NWSE, Cursor.RESIZE_NESW}) {
			Assert.assertEquals(cursor.name(), 0, GlfwWindowBridgeTest.getShape(cursor, false));
			Assert.assertNotEquals(cursor.name(), 0, GlfwWindowBridgeTest.getShape(cursor, true));
		}
		Assert.assertEquals(GLFW.GLFW_HAND_CURSOR, GlfwWindowBridgeTest.getShape(Cursor.POINTER, false));
		Assert.assertEquals(GLFW.GLFW_HRESIZE_CURSOR, GlfwWindowBridgeTest.getShape(Cursor.RESIZE_EW, false));
		Assert.assertEquals(GLFW.GLFW_VRESIZE_CURSOR, GlfwWindowBridgeTest.getShape(Cursor.RESIZE_NS, false));
	}

	@Test
	public void scalesACursorPositionToTheFramebuffer() throws Exception {
		Assert.assertEquals(300D, GlfwWindowBridgeTest.toFramebuffer(150D, 800, 1600), 0D);
		Assert.assertEquals(75D, GlfwWindowBridgeTest.toFramebuffer(150D, 1600, 800), 0D);
	}

	@Test
	public void keepsTheCursorOfAnEmptyWindow() throws Exception {
		Assert.assertEquals(150D, GlfwWindowBridgeTest.toFramebuffer(150D, 0, 1600), 0D);
	}

	private static double toFramebuffer(final double position, final int windowSize, final int framebufferSize) throws Exception {
		final Method method = GlfwWindowBridge.class.getDeclaredMethod("toFramebuffer", double.class, int.class, int.class);
		method.setAccessible(true);
		return (double) method.invoke(null, position, windowSize, framebufferSize);
	}

	private static int getShape(final Cursor cursor, final boolean extended) throws Exception {
		final Method method = GlfwWindowBridge.class.getDeclaredMethod("getShape", Cursor.class, boolean.class);
		method.setAccessible(true);
		return (int) method.invoke(null, cursor, extended);
	}

	private static Map<?, ?> cursorMap(final GlfwWindowBridge bridge) throws Exception {
		final Field field = GlfwWindowBridge.class.getDeclaredField("cursorMap");
		field.setAccessible(true);
		return (Map<?, ?>) field.get(bridge);
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