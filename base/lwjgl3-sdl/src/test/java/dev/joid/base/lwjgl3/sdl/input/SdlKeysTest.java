package dev.joid.base.lwjgl3.sdl.input;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.sdl.SDLScancode;

import dev.joid.lib.input.key.Key;

public class SdlKeysTest {

	@Test
	public void findsTheKeyAtThePlaceOfACode() {
		Assert.assertSame(Key.A, SdlKeys.getPhysicalKey(SDLScancode.SDL_SCANCODE_A));
		Assert.assertSame(Key.NUMPAD_ENTER, SdlKeys.getPhysicalKey(SDLScancode.SDL_SCANCODE_KP_ENTER));
		Assert.assertSame(Key.RIGHT_SUPER, SdlKeys.getPhysicalKey(SDLScancode.SDL_SCANCODE_RGUI));
	}

	@Test
	public void findsNoKeyForAnUnknownCode() {
		Assert.assertSame(Key.UNKNOWN, SdlKeys.getPhysicalKey(SDLScancode.SDL_SCANCODE_UNKNOWN));
		Assert.assertSame(Key.UNKNOWN, SdlKeys.getPhysicalKey(SDLScancode.SDL_SCANCODE_NONUSBACKSLASH));
	}

	@Test
	public void findsTheCodeAtBothEndsOfEveryRangeOfKeys() {
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_A, SdlKeys.getCode(Key.A));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_Z, SdlKeys.getCode(Key.Z));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_0, SdlKeys.getCode(Key.DIGIT_0));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_1, SdlKeys.getCode(Key.DIGIT_1));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_9, SdlKeys.getCode(Key.DIGIT_9));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_F1, SdlKeys.getCode(Key.F1));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_F12, SdlKeys.getCode(Key.F12));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_F13, SdlKeys.getCode(Key.F13));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_F24, SdlKeys.getCode(Key.F24));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_RIGHT, SdlKeys.getCode(Key.RIGHT));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_LEFT, SdlKeys.getCode(Key.LEFT));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_DOWN, SdlKeys.getCode(Key.DOWN));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_UP, SdlKeys.getCode(Key.UP));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_KP_0, SdlKeys.getCode(Key.NUMPAD_0));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_KP_1, SdlKeys.getCode(Key.NUMPAD_1));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_KP_9, SdlKeys.getCode(Key.NUMPAD_9));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_RETURN, SdlKeys.getCode(Key.ENTER));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_EQUALS, SdlKeys.getCode(Key.EQUAL));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_APPLICATION, SdlKeys.getCode(Key.MENU));
		Assert.assertEquals(SDLScancode.SDL_SCANCODE_UNKNOWN, SdlKeys.getCode(Key.UNKNOWN));
	}

	@Test
	public void mapsEveryCodeBackToItsKey() {
		final Set<Integer> codes = new HashSet<>();
		for (final Key key : Key.values()) {
			final int code = SdlKeys.getCode(key);
			if (code != SDLScancode.SDL_SCANCODE_UNKNOWN) {
				Assert.assertSame(key, SdlKeys.getPhysicalKey(code));
				Assert.assertTrue(codes.add(code));
			}
		}
		Assert.assertFalse(codes.isEmpty());
	}

	@Test
	public void readsAPhysicalKeyFromTheKeyState() {
		Assert.assertTrue(SdlKeys.isKeyDown(Key.SPACE, code -> code == SDLScancode.SDL_SCANCODE_SPACE));
		Assert.assertFalse(SdlKeys.isKeyDown(Key.ENTER, code -> code == SDLScancode.SDL_SCANCODE_SPACE));
		Assert.assertFalse(SdlKeys.isKeyDown(Key.UNKNOWN, code -> true));
	}

}