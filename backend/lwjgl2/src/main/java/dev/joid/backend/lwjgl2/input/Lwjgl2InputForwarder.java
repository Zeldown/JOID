package dev.joid.backend.lwjgl2.input;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.joid.backend.lwjgl2.window.Lwjgl2WindowBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2InputForwarder {

	private final Key[] pressedKeys = new Key[Keyboard.KEYBOARD_SIZE];

	public static @NonNull Lwjgl2InputForwarder create() {
		return new Lwjgl2InputForwarder();
	}

	public void poll() {
		while (Mouse.next()) {
			final int button = Mouse.getEventButton();
			if (button == -1) {
				BridgeHandler.UI.mouseMoved();
			} else if (Mouse.getEventButtonState()) {
				BridgeHandler.UI.mousePressed(MouseButton.from(button));
			} else {
				BridgeHandler.UI.mouseReleased(MouseButton.from(button));
			}

			final int wheel = Mouse.getEventDWheel();
			if (wheel != 0) {
				BridgeHandler.UI.mouseScroll(0D, wheel / 120D);
			}
		}

		while (Keyboard.next()) {
			if (!Keyboard.getEventKeyState()) {
				this.keyReleased(Keyboard.getEventKey());
				continue;
			}

			this.keyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter());
			if (Keyboard.getEventCharacter() != Keyboard.CHAR_NONE) {
				BridgeHandler.UI.charTyped(Keyboard.getEventCharacter());
			}
		}
	}

	private void keyPressed(final int code, final char character) {
		final Key key = Lwjgl2WindowBridge.getKey(code, character);
		if (code >= 0 && code < this.pressedKeys.length) {
			this.pressedKeys[code] = key;
		}
		BridgeHandler.UI.keyPressed(key);
	}

	private void keyReleased(final int code) {
		if (code < 0 || code >= this.pressedKeys.length || this.pressedKeys[code] == null) {
			BridgeHandler.UI.keyReleased(Lwjgl2WindowBridge.getKey(code));
			return;
		}

		final Key key = this.pressedKeys[code];
		this.pressedKeys[code] = null;
		BridgeHandler.UI.keyReleased(key);
	}

}