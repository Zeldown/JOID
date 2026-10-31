package dev.joid.base.lwjgl3.sdl.input;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.sdl.SDLMouse;

import dev.joid.lib.input.mouse.MouseButton;

public class SdlMouseButtonsTest {

	@Test
	public void findsTheButtonOfEveryCode() {
		Assert.assertSame(MouseButton.LEFT, SdlMouseButtons.getButton(SDLMouse.SDL_BUTTON_LEFT));
		Assert.assertSame(MouseButton.RIGHT, SdlMouseButtons.getButton(SDLMouse.SDL_BUTTON_RIGHT));
		Assert.assertSame(MouseButton.MIDDLE, SdlMouseButtons.getButton(SDLMouse.SDL_BUTTON_MIDDLE));
		Assert.assertSame(MouseButton.BACK, SdlMouseButtons.getButton(SDLMouse.SDL_BUTTON_X1));
		Assert.assertSame(MouseButton.FORWARD, SdlMouseButtons.getButton(SDLMouse.SDL_BUTTON_X2));
	}

	@Test
	public void findsNoButtonForAnUnknownCode() {
		Assert.assertSame(MouseButton.OTHER, SdlMouseButtons.getButton(0));
		Assert.assertSame(MouseButton.OTHER, SdlMouseButtons.getButton(6));
	}

}