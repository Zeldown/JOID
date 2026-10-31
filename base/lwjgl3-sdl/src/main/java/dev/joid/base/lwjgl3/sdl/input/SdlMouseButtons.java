package dev.joid.base.lwjgl3.sdl.input;

import org.lwjgl.sdl.SDLMouse;

import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SdlMouseButtons {

	public static @NonNull MouseButton getButton(final int button) {
		switch (button) {
		case SDLMouse.SDL_BUTTON_LEFT:
			return MouseButton.LEFT;
		case SDLMouse.SDL_BUTTON_RIGHT:
			return MouseButton.RIGHT;
		case SDLMouse.SDL_BUTTON_MIDDLE:
			return MouseButton.MIDDLE;
		case SDLMouse.SDL_BUTTON_X1:
			return MouseButton.BACK;
		case SDLMouse.SDL_BUTTON_X2:
			return MouseButton.FORWARD;
		default:
			return MouseButton.OTHER;
		}
	}

}