package dev.joid.backend.lwjgl2.input;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2InputForwarder {

	public static void poll() {
		while (Mouse.next()) {
			final int button = Mouse.getEventButton();
			if (button == -1) {
				BridgeHandler.UI.mouseMoved();
			} else if (Mouse.getEventButtonState()) {
				BridgeHandler.UI.mousePressed(MouseButton.of(button));
			} else {
				BridgeHandler.UI.mouseReleased(MouseButton.of(button));
			}

			final int wheel = Mouse.getEventDWheel();
			if (wheel != 0) {
				BridgeHandler.UI.mouseScroll(0D, wheel / 120D);
			}
		}

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				Lwjgl2InputForwarder.keyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter(), Keyboard.isRepeatEvent());
			} else {
				Lwjgl2InputForwarder.keyReleased(Keyboard.getEventKey());
			}
		}
	}

	private static void keyPressed(final int code, final char character, final boolean repeat) {
		if (!repeat) {
			BridgeHandler.UI.keyPressed(Lwjgl2Keys.getPhysicalKey(code), Lwjgl2Keys.getKey(code, character));
		}

		if (character != Keyboard.CHAR_NONE) {
			BridgeHandler.UI.charTyped(character);
		}
	}

	private static void keyReleased(final int code) {
		BridgeHandler.UI.keyReleased(Lwjgl2Keys.getPhysicalKey(code));
	}

}