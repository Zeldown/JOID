package dev.joid.backend.lwjgl2.input;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2InputForwarder {

	public static @NonNull Lwjgl2InputForwarder create() {
		return new Lwjgl2InputForwarder();
	}

	public void poll() {
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
				this.keyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter(), Keyboard.isRepeatEvent());
			} else {
				this.keyReleased(Keyboard.getEventKey());
			}
		}
	}

	private void keyPressed(final int code, final char character, final boolean repeat) {
		if (!repeat) {
			BridgeHandler.UI.keyPressed(Lwjgl2Keys.getPhysicalKey(code), Lwjgl2Keys.getKey(code, character));
		}

		if (character != Keyboard.CHAR_NONE) {
			BridgeHandler.UI.charTyped(character);
		}
	}

	private void keyReleased(final int code) {
		BridgeHandler.UI.keyReleased(Lwjgl2Keys.getPhysicalKey(code));
	}

}