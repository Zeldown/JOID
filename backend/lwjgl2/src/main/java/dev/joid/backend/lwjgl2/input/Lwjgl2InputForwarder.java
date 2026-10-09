package dev.joid.backend.lwjgl2.input;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.joid.backend.lwjgl2.window.Lwjgl2WindowBridge;
import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.input.mouse.MouseButton;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2InputForwarder {

	private final UIBridge bridge;

	public static @NonNull Lwjgl2InputForwarder create(final @NonNull UIBridge bridge) {
		return new Lwjgl2InputForwarder(bridge);
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
				continue;
			}

			this.keyPressed(Keyboard.getEventKey());
			if (Keyboard.getEventCharacter() != Keyboard.CHAR_NONE) {
				this.charTyped(Keyboard.getEventCharacter());
			}
		}
	}

	public boolean keyPressed(final int code) {
		return this.bridge.keyPressed(Lwjgl2WindowBridge.getKey(code));
	}

	public boolean charTyped(final int codepoint) {
		return this.bridge.charTyped(codepoint);
	}

	public boolean mouseMoved() {
		return this.bridge.mouseMoved();
	}

	public boolean mousePressed(final int button) {
		return this.bridge.mousePressed(MouseButton.from(button));
	}

	public boolean mouseReleased(final int button) {
		return this.bridge.mouseReleased(MouseButton.from(button));
	}

	public boolean mouseScrolled(final int wheel) {
		return this.bridge.mouseScroll(0D, wheel / 120D);
	}

}