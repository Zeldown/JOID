package dev.joid.base.glfw.input;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;

import dev.joid.lib.utils.key.Key;

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
		Assert.assertTrue(GlfwKeys.isPhysicalKeyDown(Key.SPACE, code -> code == GLFW.GLFW_KEY_SPACE));
		Assert.assertFalse(GlfwKeys.isPhysicalKeyDown(Key.ENTER, code -> code == GLFW.GLFW_KEY_SPACE));
		Assert.assertFalse(GlfwKeys.isPhysicalKeyDown(Key.UNKNOWN, code -> true));
	}

}