package dev.joid.base.glfw.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;

import dev.joid.lib.utils.key.Key;

public class KeyCharacterMergerTest {

	@Test
	public void mergesATextKeyWithItsCharacter() {
		final List<String> eventList = new ArrayList<>();
		final KeyCharacterMerger merger = KeyCharacterMergerTest.create(eventList);
		merger.keyPressed(Key.A, GLFW.GLFW_KEY_A, 0);
		Assert.assertTrue(eventList.isEmpty());
		merger.charTyped('a');
		Assert.assertEquals(Arrays.asList("a:A"), eventList);
		Assert.assertNull(merger.getPendingKey());
	}

	@Test
	public void sendsAControlKeyAtOnce() {
		final List<String> eventList = new ArrayList<>();
		KeyCharacterMergerTest.create(eventList).keyPressed(Key.ENTER, GLFW.GLFW_KEY_ENTER, 0);
		Assert.assertEquals(Arrays.asList("0:ENTER"), eventList);
	}

	@Test
	public void sendsAShortcutAtOnce() {
		final List<String> eventList = new ArrayList<>();
		final KeyCharacterMerger merger = KeyCharacterMergerTest.create(eventList);
		merger.keyPressed(Key.C, GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_CONTROL);
		merger.keyPressed(Key.V, GLFW.GLFW_KEY_V, GLFW.GLFW_MOD_ALT);
		Assert.assertEquals(Arrays.asList("0:C", "0:V"), eventList);
	}

	@Test
	public void sendsAPendingKeyWithoutCharacterBeforeTheNextKey() {
		final List<String> eventList = new ArrayList<>();
		final KeyCharacterMerger merger = KeyCharacterMergerTest.create(eventList);
		merger.keyPressed(Key.A, GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_SHIFT);
		merger.keyPressed(Key.B, GLFW.GLFW_KEY_B, 0);
		merger.charTyped('b');
		Assert.assertEquals(Arrays.asList("0:A", "b:B"), eventList);
	}

	@Test
	public void flushesAPendingKeyOnce() {
		final List<String> eventList = new ArrayList<>();
		final KeyCharacterMerger merger = KeyCharacterMergerTest.create(eventList);
		merger.keyPressed(Key.NUMPAD_1, GLFW.GLFW_KEY_KP_1, 0);
		merger.flush();
		merger.flush();
		Assert.assertEquals(Arrays.asList("0:NUMPAD_1"), eventList);
	}

	@Test
	public void sendsACharacterWithoutKeyAsUnknown() {
		final List<String> eventList = new ArrayList<>();
		KeyCharacterMergerTest.create(eventList).charTyped(0xE9);
		Assert.assertEquals(Arrays.asList("\u00E9:UNKNOWN"), eventList);
	}

	@Test
	public void splitsACharacterOutsideTheBasicPlane() {
		final List<Character> characterList = new ArrayList<>();
		KeyCharacterMerger.create((c, key) -> characterList.add(c)).charTyped(0x1F600);
		Assert.assertEquals(Arrays.asList('\uD83D', '\uDE00'), characterList);
	}

	private static KeyCharacterMerger create(final List<String> eventList) {
		return KeyCharacterMerger.create((c, key) -> eventList.add((c == 0 ? "0" : String.valueOf(c)) + ":" + key));
	}

}