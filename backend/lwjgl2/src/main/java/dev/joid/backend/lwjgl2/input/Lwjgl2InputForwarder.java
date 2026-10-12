package dev.joid.backend.lwjgl2.input;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.joid.backend.lwjgl2.window.Lwjgl2WindowBridge;
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
				this.mouseMoved();
			} else if (Mouse.getEventButtonState()) {
				this.mousePressed(button);
			} else {
				this.mouseReleased(button);
			}

			final int wheel = Mouse.getEventDWheel();
			if (wheel != 0) {
				this.mouseScrolled(wheel);
			}
		}

		while (Keyboard.next()) {
			if (!Keyboard.getEventKeyState()) {
				this.keyReleased(Keyboard.getEventKey());
				continue;
			}

			this.keyPressed(Keyboard.getEventKey());
			if (Keyboard.getEventCharacter() != Keyboard.CHAR_NONE) {
				this.charTyped(Keyboard.getEventCharacter());
			}
		}
	}

	public boolean keyPressed(final int code) {
		return BridgeHandler.UI.keyPressed(Lwjgl2WindowBridge.getKey(code));
	}

	public boolean keyReleased(final int code) {
		return BridgeHandler.UI.keyReleased(Lwjgl2WindowBridge.getKey(code));
	}

	public boolean charTyped(final int codepoint) {
		return BridgeHandler.UI.charTyped(codepoint);
	}

	public boolean mouseMoved() {
		return BridgeHandler.UI.mouseMoved();
	}

	public boolean mousePressed(final int button) {
		return BridgeHandler.UI.mousePressed(MouseButton.from(button));
	}

	public boolean mouseReleased(final int button) {
		return BridgeHandler.UI.mouseReleased(MouseButton.from(button));
	}

	public boolean mouseScrolled(final int wheel) {
		return BridgeHandler.UI.mouseScroll(0D, wheel / 120D);
	}

}