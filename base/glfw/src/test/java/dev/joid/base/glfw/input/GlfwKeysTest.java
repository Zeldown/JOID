package dev.joid.base.glfw.input;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;

import dev.joid.lib.input.key.Key;

public class GlfwKeysTest {

	@Test
	public void findsTheKeyAtThePlaceOfACode() {
		Assert.assertSame(Key.A, GlfwKeys.getPhysicalKey(GLFW.GLFW_KEY_A));
		Assert.assertSame(Key.NUMPAD_ENTER, GlfwKeys.getPhysicalKey(GLFW.GLFW_KEY_KP_ENTER));
		Assert.assertSame(Key.RIGHT_SUPER, GlfwKeys.getPhysicalKey(GLFW.GLFW_KEY_RIGHT_SUPER));
	}

	@Test
	public void findsNoKeyForAnUnknownCode() {
		Assert.assertSame(Key.UNKNOWN, GlfwKeys.getPhysicalKey(GLFW.GLFW_KEY_UNKNOWN));
		Assert.assertSame(Key.UNKNOWN, GlfwKeys.getPhysicalKey(GLFW.GLFW_KEY_WORLD_1));
	}

	@Test
	public void findsTheCodeOfAKey() {
		Assert.assertEquals(GLFW.GLFW_KEY_ESCAPE, GlfwKeys.getCode(Key.ESCAPE));
		Assert.assertEquals(GLFW.GLFW_KEY_KP_ADD, GlfwKeys.getCode(Key.NUMPAD_ADD));
		Assert.assertEquals(GLFW.GLFW_KEY_UNKNOWN, GlfwKeys.getCode(Key.UNKNOWN));
	}

	@Test
	public void findsTheCodeAtBothEndsOfEveryRangeOfKeys() {
		Assert.assertEquals(GLFW.GLFW_KEY_A, GlfwKeys.getCode(Key.A));
		Assert.assertEquals(GLFW.GLFW_KEY_Z, GlfwKeys.getCode(Key.Z));
		Assert.assertEquals(GLFW.GLFW_KEY_0, GlfwKeys.getCode(Key.DIGIT_0));
		Assert.assertEquals(GLFW.GLFW_KEY_9, GlfwKeys.getCode(Key.DIGIT_9));
		Assert.assertEquals(GLFW.GLFW_KEY_F1, GlfwKeys.getCode(Key.F1));
		Assert.assertEquals(GLFW.GLFW_KEY_F25, GlfwKeys.getCode(Key.F25));
		Assert.assertEquals(GLFW.GLFW_KEY_ESCAPE, GlfwKeys.getCode(Key.ESCAPE));
		Assert.assertEquals(GLFW.GLFW_KEY_END, GlfwKeys.getCode(Key.END));
		Assert.assertEquals(GLFW.GLFW_KEY_CAPS_LOCK, GlfwKeys.getCode(Key.CAPS_LOCK));
		Assert.assertEquals(GLFW.GLFW_KEY_PAUSE, GlfwKeys.getCode(Key.PAUSE));
		Assert.assertEquals(GLFW.GLFW_KEY_SPACE, GlfwKeys.getCode(Key.SPACE));
		Assert.assertEquals(GLFW.GLFW_KEY_APOSTROPHE, GlfwKeys.getCode(Key.APOSTROPHE));
		Assert.assertEquals(GLFW.GLFW_KEY_COMMA, GlfwKeys.getCode(Key.COMMA));
		Assert.assertEquals(GLFW.GLFW_KEY_SLASH, GlfwKeys.getCode(Key.SLASH));
		Assert.assertEquals(GLFW.GLFW_KEY_SEMICOLON, GlfwKeys.getCode(Key.SEMICOLON));
		Assert.assertEquals(GLFW.GLFW_KEY_EQUAL, GlfwKeys.getCode(Key.EQUAL));
		Assert.assertEquals(GLFW.GLFW_KEY_LEFT_BRACKET, GlfwKeys.getCode(Key.LEFT_BRACKET));
		Assert.assertEquals(GLFW.GLFW_KEY_RIGHT_BRACKET, GlfwKeys.getCode(Key.RIGHT_BRACKET));
		Assert.assertEquals(GLFW.GLFW_KEY_GRAVE_ACCENT, GlfwKeys.getCode(Key.GRAVE_ACCENT));
		Assert.assertEquals(GLFW.GLFW_KEY_KP_0, GlfwKeys.getCode(Key.NUMPAD_0));
		Assert.assertEquals(GLFW.GLFW_KEY_KP_EQUAL, GlfwKeys.getCode(Key.NUMPAD_EQUAL));
		Assert.assertEquals(GLFW.GLFW_KEY_LEFT_SHIFT, GlfwKeys.getCode(Key.LEFT_SHIFT));
		Assert.assertEquals(GLFW.GLFW_KEY_MENU, GlfwKeys.getCode(Key.MENU));
	}

	@Test
	public void mapsEveryCodeBackToItsKey() {
		final Set<Integer> codes = new HashSet<>();
		for (final Key key : Key.values()) {
			final int code = GlfwKeys.getCode(key);
			if (code != GLFW.GLFW_KEY_UNKNOWN) {
				Assert.assertSame(key, GlfwKeys.getPhysicalKey(code));
				Assert.assertTrue(codes.add(code));
			}
		}
		Assert.assertFalse(codes.isEmpty());
	}

	@Test
	public void readsAPhysicalKeyFromTheKeyState() {
		Assert.assertTrue(GlfwKeys.isKeyDown(Key.SPACE, code -> code == GLFW.GLFW_KEY_SPACE));
		Assert.assertFalse(GlfwKeys.isKeyDown(Key.ENTER, code -> code == GLFW.GLFW_KEY_SPACE));
		Assert.assertFalse(GlfwKeys.isKeyDown(Key.UNKNOWN, code -> true));
	}

}