package dev.joid.backend.lwjgl2.window;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import dev.joid.backend.lwjgl2.window.cursor.NativeCursor;
import dev.joid.backend.lwjgl2.window.cursor.NativeCursors;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.cursor.Cursor;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class WindowBridge implements IWindowBridge {

	private static final Key[]             KEY_ARRAY = new Key[Keyboard.KEYBOARD_SIZE];
	private static final Map<Key, Integer> CODE_MAP  = new EnumMap<>(Key.class);

	static {
		Arrays.fill(WindowBridge.KEY_ARRAY, Key.UNKNOWN);
		WindowBridge.map(Key.A, Keyboard.KEY_A);
		WindowBridge.map(Key.B, Keyboard.KEY_B);
		WindowBridge.map(Key.C, Keyboard.KEY_C);
		WindowBridge.map(Key.D, Keyboard.KEY_D);
		WindowBridge.map(Key.E, Keyboard.KEY_E);
		WindowBridge.map(Key.F, Keyboard.KEY_F);
		WindowBridge.map(Key.G, Keyboard.KEY_G);
		WindowBridge.map(Key.H, Keyboard.KEY_H);
		WindowBridge.map(Key.I, Keyboard.KEY_I);
		WindowBridge.map(Key.J, Keyboard.KEY_J);
		WindowBridge.map(Key.K, Keyboard.KEY_K);
		WindowBridge.map(Key.L, Keyboard.KEY_L);
		WindowBridge.map(Key.M, Keyboard.KEY_M);
		WindowBridge.map(Key.N, Keyboard.KEY_N);
		WindowBridge.map(Key.O, Keyboard.KEY_O);
		WindowBridge.map(Key.P, Keyboard.KEY_P);
		WindowBridge.map(Key.Q, Keyboard.KEY_Q);
		WindowBridge.map(Key.R, Keyboard.KEY_R);
		WindowBridge.map(Key.S, Keyboard.KEY_S);
		WindowBridge.map(Key.T, Keyboard.KEY_T);
		WindowBridge.map(Key.U, Keyboard.KEY_U);
		WindowBridge.map(Key.V, Keyboard.KEY_V);
		WindowBridge.map(Key.W, Keyboard.KEY_W);
		WindowBridge.map(Key.X, Keyboard.KEY_X);
		WindowBridge.map(Key.Y, Keyboard.KEY_Y);
		WindowBridge.map(Key.Z, Keyboard.KEY_Z);
		WindowBridge.map(Key.DIGIT_0, Keyboard.KEY_0);
		WindowBridge.map(Key.DIGIT_1, Keyboard.KEY_1);
		WindowBridge.map(Key.DIGIT_2, Keyboard.KEY_2);
		WindowBridge.map(Key.DIGIT_3, Keyboard.KEY_3);
		WindowBridge.map(Key.DIGIT_4, Keyboard.KEY_4);
		WindowBridge.map(Key.DIGIT_5, Keyboard.KEY_5);
		WindowBridge.map(Key.DIGIT_6, Keyboard.KEY_6);
		WindowBridge.map(Key.DIGIT_7, Keyboard.KEY_7);
		WindowBridge.map(Key.DIGIT_8, Keyboard.KEY_8);
		WindowBridge.map(Key.DIGIT_9, Keyboard.KEY_9);
		WindowBridge.map(Key.F1, Keyboard.KEY_F1);
		WindowBridge.map(Key.F2, Keyboard.KEY_F2);
		WindowBridge.map(Key.F3, Keyboard.KEY_F3);
		WindowBridge.map(Key.F4, Keyboard.KEY_F4);
		WindowBridge.map(Key.F5, Keyboard.KEY_F5);
		WindowBridge.map(Key.F6, Keyboard.KEY_F6);
		WindowBridge.map(Key.F7, Keyboard.KEY_F7);
		WindowBridge.map(Key.F8, Keyboard.KEY_F8);
		WindowBridge.map(Key.F9, Keyboard.KEY_F9);
		WindowBridge.map(Key.F10, Keyboard.KEY_F10);
		WindowBridge.map(Key.F11, Keyboard.KEY_F11);
		WindowBridge.map(Key.F12, Keyboard.KEY_F12);
		WindowBridge.map(Key.F13, Keyboard.KEY_F13);
		WindowBridge.map(Key.F14, Keyboard.KEY_F14);
		WindowBridge.map(Key.F15, Keyboard.KEY_F15);
		WindowBridge.map(Key.F16, Keyboard.KEY_F16);
		WindowBridge.map(Key.F17, Keyboard.KEY_F17);
		WindowBridge.map(Key.F18, Keyboard.KEY_F18);
		WindowBridge.map(Key.F19, Keyboard.KEY_F19);
		WindowBridge.map(Key.ESCAPE, Keyboard.KEY_ESCAPE);
		WindowBridge.map(Key.ENTER, Keyboard.KEY_RETURN);
		WindowBridge.map(Key.TAB, Keyboard.KEY_TAB);
		WindowBridge.map(Key.BACKSPACE, Keyboard.KEY_BACK);
		WindowBridge.map(Key.INSERT, Keyboard.KEY_INSERT);
		WindowBridge.map(Key.DELETE, Keyboard.KEY_DELETE);
		WindowBridge.map(Key.RIGHT, Keyboard.KEY_RIGHT);
		WindowBridge.map(Key.LEFT, Keyboard.KEY_LEFT);
		WindowBridge.map(Key.DOWN, Keyboard.KEY_DOWN);
		WindowBridge.map(Key.UP, Keyboard.KEY_UP);
		WindowBridge.map(Key.PAGE_UP, Keyboard.KEY_PRIOR);
		WindowBridge.map(Key.PAGE_DOWN, Keyboard.KEY_NEXT);
		WindowBridge.map(Key.HOME, Keyboard.KEY_HOME);
		WindowBridge.map(Key.END, Keyboard.KEY_END);
		WindowBridge.map(Key.CAPS_LOCK, Keyboard.KEY_CAPITAL);
		WindowBridge.map(Key.SCROLL_LOCK, Keyboard.KEY_SCROLL);
		WindowBridge.map(Key.NUM_LOCK, Keyboard.KEY_NUMLOCK);
		WindowBridge.map(Key.PRINT_SCREEN, Keyboard.KEY_SYSRQ);
		WindowBridge.map(Key.PAUSE, Keyboard.KEY_PAUSE);
		WindowBridge.map(Key.SPACE, Keyboard.KEY_SPACE);
		WindowBridge.map(Key.APOSTROPHE, Keyboard.KEY_APOSTROPHE);
		WindowBridge.map(Key.COMMA, Keyboard.KEY_COMMA);
		WindowBridge.map(Key.MINUS, Keyboard.KEY_MINUS);
		WindowBridge.map(Key.PERIOD, Keyboard.KEY_PERIOD);
		WindowBridge.map(Key.SLASH, Keyboard.KEY_SLASH);
		WindowBridge.map(Key.SEMICOLON, Keyboard.KEY_SEMICOLON);
		WindowBridge.map(Key.EQUAL, Keyboard.KEY_EQUALS);
		WindowBridge.map(Key.LEFT_BRACKET, Keyboard.KEY_LBRACKET);
		WindowBridge.map(Key.BACKSLASH, Keyboard.KEY_BACKSLASH);
		WindowBridge.map(Key.RIGHT_BRACKET, Keyboard.KEY_RBRACKET);
		WindowBridge.map(Key.GRAVE_ACCENT, Keyboard.KEY_GRAVE);
		WindowBridge.map(Key.NUMPAD_0, Keyboard.KEY_NUMPAD0);
		WindowBridge.map(Key.NUMPAD_1, Keyboard.KEY_NUMPAD1);
		WindowBridge.map(Key.NUMPAD_2, Keyboard.KEY_NUMPAD2);
		WindowBridge.map(Key.NUMPAD_3, Keyboard.KEY_NUMPAD3);
		WindowBridge.map(Key.NUMPAD_4, Keyboard.KEY_NUMPAD4);
		WindowBridge.map(Key.NUMPAD_5, Keyboard.KEY_NUMPAD5);
		WindowBridge.map(Key.NUMPAD_6, Keyboard.KEY_NUMPAD6);
		WindowBridge.map(Key.NUMPAD_7, Keyboard.KEY_NUMPAD7);
		WindowBridge.map(Key.NUMPAD_8, Keyboard.KEY_NUMPAD8);
		WindowBridge.map(Key.NUMPAD_9, Keyboard.KEY_NUMPAD9);
		WindowBridge.map(Key.NUMPAD_DECIMAL, Keyboard.KEY_DECIMAL);
		WindowBridge.map(Key.NUMPAD_DIVIDE, Keyboard.KEY_DIVIDE);
		WindowBridge.map(Key.NUMPAD_MULTIPLY, Keyboard.KEY_MULTIPLY);
		WindowBridge.map(Key.NUMPAD_SUBTRACT, Keyboard.KEY_SUBTRACT);
		WindowBridge.map(Key.NUMPAD_ADD, Keyboard.KEY_ADD);
		WindowBridge.map(Key.NUMPAD_ENTER, Keyboard.KEY_NUMPADENTER);
		WindowBridge.map(Key.NUMPAD_EQUAL, Keyboard.KEY_NUMPADEQUALS);
		WindowBridge.map(Key.LEFT_SHIFT, Keyboard.KEY_LSHIFT);
		WindowBridge.map(Key.LEFT_CONTROL, Keyboard.KEY_LCONTROL);
		WindowBridge.map(Key.LEFT_ALT, Keyboard.KEY_LMENU);
		WindowBridge.map(Key.LEFT_SUPER, Keyboard.KEY_LMETA);
		WindowBridge.map(Key.RIGHT_SHIFT, Keyboard.KEY_RSHIFT);
		WindowBridge.map(Key.RIGHT_CONTROL, Keyboard.KEY_RCONTROL);
		WindowBridge.map(Key.RIGHT_ALT, Keyboard.KEY_RMENU);
		WindowBridge.map(Key.RIGHT_SUPER, Keyboard.KEY_RMETA);
		WindowBridge.map(Key.MENU, Keyboard.KEY_APPS);
	}

	@Override
	public int getWidth() {
		return Display.getWidth();
	}

	@Override
	public int getHeight() {
		return Display.getHeight();
	}

	@Override
	public double getMouseX() {
		return Mouse.getX();
	}

	@Override
	public double getMouseY() {
		return Display.getHeight() - Mouse.getY();
	}

	@Override
	public @NonNull String getClipboard() {
		try {
			final Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (final Exception silent) {}

		return "";
	}

	public static @NonNull Key getKey(final int code) {
		return code < 0 || code >= WindowBridge.KEY_ARRAY.length ? Key.UNKNOWN : WindowBridge.KEY_ARRAY[code];
	}

	@Override
	public boolean isMouseGrabbed() {
		return Mouse.isGrabbed();
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		final Integer code = WindowBridge.CODE_MAP.get(key);
		return code != null && Keyboard.isKeyDown(code);
	}

	@Override
	public void setCursor(final @NonNull Cursor cursor) {
		final NativeCursor nativeCursor = NativeCursors.get();
		if (nativeCursor != null && !Mouse.isGrabbed()) {
			nativeCursor.apply(cursor);
		}
	}

	@Override
	public void setClipboard(final @NonNull String text) {
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
		} catch (final Exception silent) {}
	}

	private static void map(final Key key, final int code) {
		WindowBridge.CODE_MAP.put(key, code);
		WindowBridge.KEY_ARRAY[code] = key;
	}

}