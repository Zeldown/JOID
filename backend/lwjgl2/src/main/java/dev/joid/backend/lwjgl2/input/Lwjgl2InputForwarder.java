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
			if (Keyboard.getEventKeyState()) {
				this.keyPressed(Keyboard.getEventCharacter(), Keyboard.getEventKey());
			}
		}
	}

	public boolean keyPressed(final char character, final int code) {
		return this.bridge.keyTyped(character, Lwjgl2WindowBridge.getKey(code));
	}

	public boolean mousePressed(final int button) {
		return this.bridge.mousePressed(MouseButton.from(button));
	}

	public boolean mouseReleased(final int button) {
		return this.bridge.mouseReleased(MouseButton.from(button));
	}

	public boolean mouseMoved() {
		return this.bridge.mouseMoved();
	}

	public boolean mouseScrolled(final int wheel) {
		return this.bridge.mouseScroll(0D, wheel / 120D);
	}

}