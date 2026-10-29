package dev.joid.base.lwjgl2.input;

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
			Lwjgl2InputForwarder.forwardMouseEvent();
		}

		while (Keyboard.next()) {
			Lwjgl2InputForwarder.forwardKeyboardEvent();
		}
	}

	public static boolean forwardMouseEvent() {
		final int button = Mouse.getEventButton();
		final int wheel = Mouse.getEventDWheel();
		final boolean consumed = button == -1 ? wheel == 0 && BridgeHandler.UI.mouseMoved() : Mouse.getEventButtonState() ? BridgeHandler.UI.mousePressed(MouseButton.of(button)) : BridgeHandler.UI.mouseReleased(MouseButton.of(button));
		return wheel != 0 && BridgeHandler.UI.mouseScroll(0D, wheel / 120D) || consumed;
	}

	public static boolean forwardKeyboardEvent() {
		final int code = Keyboard.getEventKey();
		final char character = Keyboard.getEventCharacter();
		if (code == Keyboard.KEY_NONE) {
			return character != Keyboard.CHAR_NONE && BridgeHandler.UI.charTyped(character);
		}

		if (!Keyboard.getEventKeyState()) {
			return BridgeHandler.UI.keyReleased(Lwjgl2Keys.getPhysicalKey(code));
		}

		final boolean consumed = Keyboard.isRepeatEvent() ? BridgeHandler.UI.isKeyConsumed(Lwjgl2Keys.getPhysicalKey(code)) : BridgeHandler.UI.keyPressed(Lwjgl2Keys.getPhysicalKey(code), Lwjgl2Keys.getKey(code, character));
		return character != Keyboard.CHAR_NONE && BridgeHandler.UI.charTyped(character) || consumed;
	}

}