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

public final class Lwjgl2WindowBridge implements IWindowBridge {

	private static final Key[]             KEY_ARRAY = new Key[Keyboard.KEYBOARD_SIZE];
	private static final Map<Key, Integer> CODE_MAP  = new EnumMap<>(Key.class);

	static {
		Arrays.fill(Lwjgl2WindowBridge.KEY_ARRAY, Key.UNKNOWN);
		Lwjgl2WindowBridge.map(Key.A, Keyboard.KEY_A);
		Lwjgl2WindowBridge.map(Key.B, Keyboard.KEY_B);
		Lwjgl2WindowBridge.map(Key.C, Keyboard.KEY_C);
		Lwjgl2WindowBridge.map(Key.D, Keyboard.KEY_D);
		Lwjgl2WindowBridge.map(Key.E, Keyboard.KEY_E);
		Lwjgl2WindowBridge.map(Key.F, Keyboard.KEY_F);
		Lwjgl2WindowBridge.map(Key.G, Keyboard.KEY_G);
		Lwjgl2WindowBridge.map(Key.H, Keyboard.KEY_H);
		Lwjgl2WindowBridge.map(Key.I, Keyboard.KEY_I);
		Lwjgl2WindowBridge.map(Key.J, Keyboard.KEY_J);
		Lwjgl2WindowBridge.map(Key.K, Keyboard.KEY_K);
		Lwjgl2WindowBridge.map(Key.L, Keyboard.KEY_L);
		Lwjgl2WindowBridge.map(Key.M, Keyboard.KEY_M);
		Lwjgl2WindowBridge.map(Key.N, Keyboard.KEY_N);
		Lwjgl2WindowBridge.map(Key.O, Keyboard.KEY_O);
		Lwjgl2WindowBridge.map(Key.P, Keyboard.KEY_P);
		Lwjgl2WindowBridge.map(Key.Q, Keyboard.KEY_Q);
		Lwjgl2WindowBridge.map(Key.R, Keyboard.KEY_R);
		Lwjgl2WindowBridge.map(Key.S, Keyboard.KEY_S);
		Lwjgl2WindowBridge.map(Key.T, Keyboard.KEY_T);
		Lwjgl2WindowBridge.map(Key.U, Keyboard.KEY_U);
		Lwjgl2WindowBridge.map(Key.V, Keyboard.KEY_V);
		Lwjgl2WindowBridge.map(Key.W, Keyboard.KEY_W);
		Lwjgl2WindowBridge.map(Key.X, Keyboard.KEY_X);
		Lwjgl2WindowBridge.map(Key.Y, Keyboard.KEY_Y);
		Lwjgl2WindowBridge.map(Key.Z, Keyboard.KEY_Z);
		Lwjgl2WindowBridge.map(Key.DIGIT_0, Keyboard.KEY_0);
		Lwjgl2WindowBridge.map(Key.DIGIT_1, Keyboard.KEY_1);
		Lwjgl2WindowBridge.map(Key.DIGIT_2, Keyboard.KEY_2);
		Lwjgl2WindowBridge.map(Key.DIGIT_3, Keyboard.KEY_3);
		Lwjgl2WindowBridge.map(Key.DIGIT_4, Keyboard.KEY_4);
		Lwjgl2WindowBridge.map(Key.DIGIT_5, Keyboard.KEY_5);
		Lwjgl2WindowBridge.map(Key.DIGIT_6, Keyboard.KEY_6);
		Lwjgl2WindowBridge.map(Key.DIGIT_7, Keyboard.KEY_7);
		Lwjgl2WindowBridge.map(Key.DIGIT_8, Keyboard.KEY_8);
		Lwjgl2WindowBridge.map(Key.DIGIT_9, Keyboard.KEY_9);
		Lwjgl2WindowBridge.map(Key.F1, Keyboard.KEY_F1);
		Lwjgl2WindowBridge.map(Key.F2, Keyboard.KEY_F2);
		Lwjgl2WindowBridge.map(Key.F3, Keyboard.KEY_F3);
		Lwjgl2WindowBridge.map(Key.F4, Keyboard.KEY_F4);
		Lwjgl2WindowBridge.map(Key.F5, Keyboard.KEY_F5);
		Lwjgl2WindowBridge.map(Key.F6, Keyboard.KEY_F6);
		Lwjgl2WindowBridge.map(Key.F7, Keyboard.KEY_F7);
		Lwjgl2WindowBridge.map(Key.F8, Keyboard.KEY_F8);
		Lwjgl2WindowBridge.map(Key.F9, Keyboard.KEY_F9);
		Lwjgl2WindowBridge.map(Key.F10, Keyboard.KEY_F10);
		Lwjgl2WindowBridge.map(Key.F11, Keyboard.KEY_F11);
		Lwjgl2WindowBridge.map(Key.F12, Keyboard.KEY_F12);
		Lwjgl2WindowBridge.map(Key.F13, Keyboard.KEY_F13);
		Lwjgl2WindowBridge.map(Key.F14, Keyboard.KEY_F14);
		Lwjgl2WindowBridge.map(Key.F15, Keyboard.KEY_F15);
		Lwjgl2WindowBridge.map(Key.F16, Keyboard.KEY_F16);
		Lwjgl2WindowBridge.map(Key.F17, Keyboard.KEY_F17);
		Lwjgl2WindowBridge.map(Key.F18, Keyboard.KEY_F18);
		Lwjgl2WindowBridge.map(Key.F19, Keyboard.KEY_F19);
		Lwjgl2WindowBridge.map(Key.ESCAPE, Keyboard.KEY_ESCAPE);
		Lwjgl2WindowBridge.map(Key.ENTER, Keyboard.KEY_RETURN);
		Lwjgl2WindowBridge.map(Key.TAB, Keyboard.KEY_TAB);
		Lwjgl2WindowBridge.map(Key.BACKSPACE, Keyboard.KEY_BACK);
		Lwjgl2WindowBridge.map(Key.INSERT, Keyboard.KEY_INSERT);
		Lwjgl2WindowBridge.map(Key.DELETE, Keyboard.KEY_DELETE);
		Lwjgl2WindowBridge.map(Key.RIGHT, Keyboard.KEY_RIGHT);
		Lwjgl2WindowBridge.map(Key.LEFT, Keyboard.KEY_LEFT);
		Lwjgl2WindowBridge.map(Key.DOWN, Keyboard.KEY_DOWN);
		Lwjgl2WindowBridge.map(Key.UP, Keyboard.KEY_UP);
		Lwjgl2WindowBridge.map(Key.PAGE_UP, Keyboard.KEY_PRIOR);
		Lwjgl2WindowBridge.map(Key.PAGE_DOWN, Keyboard.KEY_NEXT);
		Lwjgl2WindowBridge.map(Key.HOME, Keyboard.KEY_HOME);
		Lwjgl2WindowBridge.map(Key.END, Keyboard.KEY_END);
		Lwjgl2WindowBridge.map(Key.CAPS_LOCK, Keyboard.KEY_CAPITAL);
		Lwjgl2WindowBridge.map(Key.SCROLL_LOCK, Keyboard.KEY_SCROLL);
		Lwjgl2WindowBridge.map(Key.NUM_LOCK, Keyboard.KEY_NUMLOCK);
		Lwjgl2WindowBridge.map(Key.PRINT_SCREEN, Keyboard.KEY_SYSRQ);
		Lwjgl2WindowBridge.map(Key.PAUSE, Keyboard.KEY_PAUSE);
		Lwjgl2WindowBridge.map(Key.SPACE, Keyboard.KEY_SPACE);
		Lwjgl2WindowBridge.map(Key.APOSTROPHE, Keyboard.KEY_APOSTROPHE);
		Lwjgl2WindowBridge.map(Key.COMMA, Keyboard.KEY_COMMA);
		Lwjgl2WindowBridge.map(Key.MINUS, Keyboard.KEY_MINUS);
		Lwjgl2WindowBridge.map(Key.PERIOD, Keyboard.KEY_PERIOD);
		Lwjgl2WindowBridge.map(Key.SLASH, Keyboard.KEY_SLASH);
		Lwjgl2WindowBridge.map(Key.SEMICOLON, Keyboard.KEY_SEMICOLON);
		Lwjgl2WindowBridge.map(Key.EQUAL, Keyboard.KEY_EQUALS);
		Lwjgl2WindowBridge.map(Key.LEFT_BRACKET, Keyboard.KEY_LBRACKET);
		Lwjgl2WindowBridge.map(Key.BACKSLASH, Keyboard.KEY_BACKSLASH);
		Lwjgl2WindowBridge.map(Key.RIGHT_BRACKET, Keyboard.KEY_RBRACKET);
		Lwjgl2WindowBridge.map(Key.GRAVE_ACCENT, Keyboard.KEY_GRAVE);
		Lwjgl2WindowBridge.map(Key.NUMPAD_0, Keyboard.KEY_NUMPAD0);
		Lwjgl2WindowBridge.map(Key.NUMPAD_1, Keyboard.KEY_NUMPAD1);
		Lwjgl2WindowBridge.map(Key.NUMPAD_2, Keyboard.KEY_NUMPAD2);
		Lwjgl2WindowBridge.map(Key.NUMPAD_3, Keyboard.KEY_NUMPAD3);
		Lwjgl2WindowBridge.map(Key.NUMPAD_4, Keyboard.KEY_NUMPAD4);
		Lwjgl2WindowBridge.map(Key.NUMPAD_5, Keyboard.KEY_NUMPAD5);
		Lwjgl2WindowBridge.map(Key.NUMPAD_6, Keyboard.KEY_NUMPAD6);
		Lwjgl2WindowBridge.map(Key.NUMPAD_7, Keyboard.KEY_NUMPAD7);
		Lwjgl2WindowBridge.map(Key.NUMPAD_8, Keyboard.KEY_NUMPAD8);
		Lwjgl2WindowBridge.map(Key.NUMPAD_9, Keyboard.KEY_NUMPAD9);
		Lwjgl2WindowBridge.map(Key.NUMPAD_DECIMAL, Keyboard.KEY_DECIMAL);
		Lwjgl2WindowBridge.map(Key.NUMPAD_DIVIDE, Keyboard.KEY_DIVIDE);
		Lwjgl2WindowBridge.map(Key.NUMPAD_MULTIPLY, Keyboard.KEY_MULTIPLY);
		Lwjgl2WindowBridge.map(Key.NUMPAD_SUBTRACT, Keyboard.KEY_SUBTRACT);
		Lwjgl2WindowBridge.map(Key.NUMPAD_ADD, Keyboard.KEY_ADD);
		Lwjgl2WindowBridge.map(Key.NUMPAD_ENTER, Keyboard.KEY_NUMPADENTER);
		Lwjgl2WindowBridge.map(Key.NUMPAD_EQUAL, Keyboard.KEY_NUMPADEQUALS);
		Lwjgl2WindowBridge.map(Key.LEFT_SHIFT, Keyboard.KEY_LSHIFT);
		Lwjgl2WindowBridge.map(Key.LEFT_CONTROL, Keyboard.KEY_LCONTROL);
		Lwjgl2WindowBridge.map(Key.LEFT_ALT, Keyboard.KEY_LMENU);
		Lwjgl2WindowBridge.map(Key.LEFT_SUPER, Keyboard.KEY_LMETA);
		Lwjgl2WindowBridge.map(Key.RIGHT_SHIFT, Keyboard.KEY_RSHIFT);
		Lwjgl2WindowBridge.map(Key.RIGHT_CONTROL, Keyboard.KEY_RCONTROL);
		Lwjgl2WindowBridge.map(Key.RIGHT_ALT, Keyboard.KEY_RMENU);
		Lwjgl2WindowBridge.map(Key.RIGHT_SUPER, Keyboard.KEY_RMETA);
		Lwjgl2WindowBridge.map(Key.MENU, Keyboard.KEY_APPS);
	}

	@Override
	public int getWidth() {
		return Math.round(Display.getWidth() * Display.getPixelScaleFactor());
	}

	@Override
	public int getHeight() {
		return Math.round(Display.getHeight() * Display.getPixelScaleFactor());
	}

	@Override
	public double getMouseX() {
		return Mouse.getX() * (double) Display.getPixelScaleFactor();
	}

	@Override
	public double getMouseY() {
		return (Display.getHeight() - Mouse.getY()) * (double) Display.getPixelScaleFactor();
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
		return code < 0 || code >= Lwjgl2WindowBridge.KEY_ARRAY.length ? Key.UNKNOWN : Lwjgl2WindowBridge.KEY_ARRAY[code];
	}

	@Override
	public boolean isMouseGrabbed() {
		return Mouse.isGrabbed();
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		final Integer code = Lwjgl2WindowBridge.CODE_MAP.get(key);
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
		Lwjgl2WindowBridge.CODE_MAP.put(key, code);
		Lwjgl2WindowBridge.KEY_ARRAY[code] = key;
	}

}