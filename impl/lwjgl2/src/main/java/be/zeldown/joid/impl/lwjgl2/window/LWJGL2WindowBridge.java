package be.zeldown.joid.impl.lwjgl2.window;

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

import be.zeldown.joid.lib.bridge.window.IWindowBridge;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class LWJGL2WindowBridge implements IWindowBridge {

	private static final Map<Key, Integer> CODE_MAP  = new EnumMap<>(Key.class);
	private static final Key[]             KEY_ARRAY = new Key[Keyboard.KEYBOARD_SIZE];

	static {
		Arrays.fill(LWJGL2WindowBridge.KEY_ARRAY, Key.UNKNOWN);
		LWJGL2WindowBridge.map(Key.A, Keyboard.KEY_A);
		LWJGL2WindowBridge.map(Key.B, Keyboard.KEY_B);
		LWJGL2WindowBridge.map(Key.C, Keyboard.KEY_C);
		LWJGL2WindowBridge.map(Key.D, Keyboard.KEY_D);
		LWJGL2WindowBridge.map(Key.E, Keyboard.KEY_E);
		LWJGL2WindowBridge.map(Key.F, Keyboard.KEY_F);
		LWJGL2WindowBridge.map(Key.G, Keyboard.KEY_G);
		LWJGL2WindowBridge.map(Key.H, Keyboard.KEY_H);
		LWJGL2WindowBridge.map(Key.I, Keyboard.KEY_I);
		LWJGL2WindowBridge.map(Key.J, Keyboard.KEY_J);
		LWJGL2WindowBridge.map(Key.K, Keyboard.KEY_K);
		LWJGL2WindowBridge.map(Key.L, Keyboard.KEY_L);
		LWJGL2WindowBridge.map(Key.M, Keyboard.KEY_M);
		LWJGL2WindowBridge.map(Key.N, Keyboard.KEY_N);
		LWJGL2WindowBridge.map(Key.O, Keyboard.KEY_O);
		LWJGL2WindowBridge.map(Key.P, Keyboard.KEY_P);
		LWJGL2WindowBridge.map(Key.Q, Keyboard.KEY_Q);
		LWJGL2WindowBridge.map(Key.R, Keyboard.KEY_R);
		LWJGL2WindowBridge.map(Key.S, Keyboard.KEY_S);
		LWJGL2WindowBridge.map(Key.T, Keyboard.KEY_T);
		LWJGL2WindowBridge.map(Key.U, Keyboard.KEY_U);
		LWJGL2WindowBridge.map(Key.V, Keyboard.KEY_V);
		LWJGL2WindowBridge.map(Key.W, Keyboard.KEY_W);
		LWJGL2WindowBridge.map(Key.X, Keyboard.KEY_X);
		LWJGL2WindowBridge.map(Key.Y, Keyboard.KEY_Y);
		LWJGL2WindowBridge.map(Key.Z, Keyboard.KEY_Z);
		LWJGL2WindowBridge.map(Key.DIGIT_0, Keyboard.KEY_0);
		LWJGL2WindowBridge.map(Key.DIGIT_1, Keyboard.KEY_1);
		LWJGL2WindowBridge.map(Key.DIGIT_2, Keyboard.KEY_2);
		LWJGL2WindowBridge.map(Key.DIGIT_3, Keyboard.KEY_3);
		LWJGL2WindowBridge.map(Key.DIGIT_4, Keyboard.KEY_4);
		LWJGL2WindowBridge.map(Key.DIGIT_5, Keyboard.KEY_5);
		LWJGL2WindowBridge.map(Key.DIGIT_6, Keyboard.KEY_6);
		LWJGL2WindowBridge.map(Key.DIGIT_7, Keyboard.KEY_7);
		LWJGL2WindowBridge.map(Key.DIGIT_8, Keyboard.KEY_8);
		LWJGL2WindowBridge.map(Key.DIGIT_9, Keyboard.KEY_9);
		LWJGL2WindowBridge.map(Key.F1, Keyboard.KEY_F1);
		LWJGL2WindowBridge.map(Key.F2, Keyboard.KEY_F2);
		LWJGL2WindowBridge.map(Key.F3, Keyboard.KEY_F3);
		LWJGL2WindowBridge.map(Key.F4, Keyboard.KEY_F4);
		LWJGL2WindowBridge.map(Key.F5, Keyboard.KEY_F5);
		LWJGL2WindowBridge.map(Key.F6, Keyboard.KEY_F6);
		LWJGL2WindowBridge.map(Key.F7, Keyboard.KEY_F7);
		LWJGL2WindowBridge.map(Key.F8, Keyboard.KEY_F8);
		LWJGL2WindowBridge.map(Key.F9, Keyboard.KEY_F9);
		LWJGL2WindowBridge.map(Key.F10, Keyboard.KEY_F10);
		LWJGL2WindowBridge.map(Key.F11, Keyboard.KEY_F11);
		LWJGL2WindowBridge.map(Key.F12, Keyboard.KEY_F12);
		LWJGL2WindowBridge.map(Key.F13, Keyboard.KEY_F13);
		LWJGL2WindowBridge.map(Key.F14, Keyboard.KEY_F14);
		LWJGL2WindowBridge.map(Key.F15, Keyboard.KEY_F15);
		LWJGL2WindowBridge.map(Key.F16, Keyboard.KEY_F16);
		LWJGL2WindowBridge.map(Key.F17, Keyboard.KEY_F17);
		LWJGL2WindowBridge.map(Key.F18, Keyboard.KEY_F18);
		LWJGL2WindowBridge.map(Key.F19, Keyboard.KEY_F19);
		LWJGL2WindowBridge.map(Key.ESCAPE, Keyboard.KEY_ESCAPE);
		LWJGL2WindowBridge.map(Key.ENTER, Keyboard.KEY_RETURN);
		LWJGL2WindowBridge.map(Key.TAB, Keyboard.KEY_TAB);
		LWJGL2WindowBridge.map(Key.BACKSPACE, Keyboard.KEY_BACK);
		LWJGL2WindowBridge.map(Key.INSERT, Keyboard.KEY_INSERT);
		LWJGL2WindowBridge.map(Key.DELETE, Keyboard.KEY_DELETE);
		LWJGL2WindowBridge.map(Key.RIGHT, Keyboard.KEY_RIGHT);
		LWJGL2WindowBridge.map(Key.LEFT, Keyboard.KEY_LEFT);
		LWJGL2WindowBridge.map(Key.DOWN, Keyboard.KEY_DOWN);
		LWJGL2WindowBridge.map(Key.UP, Keyboard.KEY_UP);
		LWJGL2WindowBridge.map(Key.PAGE_UP, Keyboard.KEY_PRIOR);
		LWJGL2WindowBridge.map(Key.PAGE_DOWN, Keyboard.KEY_NEXT);
		LWJGL2WindowBridge.map(Key.HOME, Keyboard.KEY_HOME);
		LWJGL2WindowBridge.map(Key.END, Keyboard.KEY_END);
		LWJGL2WindowBridge.map(Key.CAPS_LOCK, Keyboard.KEY_CAPITAL);
		LWJGL2WindowBridge.map(Key.SCROLL_LOCK, Keyboard.KEY_SCROLL);
		LWJGL2WindowBridge.map(Key.NUM_LOCK, Keyboard.KEY_NUMLOCK);
		LWJGL2WindowBridge.map(Key.PRINT_SCREEN, Keyboard.KEY_SYSRQ);
		LWJGL2WindowBridge.map(Key.PAUSE, Keyboard.KEY_PAUSE);
		LWJGL2WindowBridge.map(Key.SPACE, Keyboard.KEY_SPACE);
		LWJGL2WindowBridge.map(Key.APOSTROPHE, Keyboard.KEY_APOSTROPHE);
		LWJGL2WindowBridge.map(Key.COMMA, Keyboard.KEY_COMMA);
		LWJGL2WindowBridge.map(Key.MINUS, Keyboard.KEY_MINUS);
		LWJGL2WindowBridge.map(Key.PERIOD, Keyboard.KEY_PERIOD);
		LWJGL2WindowBridge.map(Key.SLASH, Keyboard.KEY_SLASH);
		LWJGL2WindowBridge.map(Key.SEMICOLON, Keyboard.KEY_SEMICOLON);
		LWJGL2WindowBridge.map(Key.EQUAL, Keyboard.KEY_EQUALS);
		LWJGL2WindowBridge.map(Key.LEFT_BRACKET, Keyboard.KEY_LBRACKET);
		LWJGL2WindowBridge.map(Key.BACKSLASH, Keyboard.KEY_BACKSLASH);
		LWJGL2WindowBridge.map(Key.RIGHT_BRACKET, Keyboard.KEY_RBRACKET);
		LWJGL2WindowBridge.map(Key.GRAVE_ACCENT, Keyboard.KEY_GRAVE);
		LWJGL2WindowBridge.map(Key.NUMPAD_0, Keyboard.KEY_NUMPAD0);
		LWJGL2WindowBridge.map(Key.NUMPAD_1, Keyboard.KEY_NUMPAD1);
		LWJGL2WindowBridge.map(Key.NUMPAD_2, Keyboard.KEY_NUMPAD2);
		LWJGL2WindowBridge.map(Key.NUMPAD_3, Keyboard.KEY_NUMPAD3);
		LWJGL2WindowBridge.map(Key.NUMPAD_4, Keyboard.KEY_NUMPAD4);
		LWJGL2WindowBridge.map(Key.NUMPAD_5, Keyboard.KEY_NUMPAD5);
		LWJGL2WindowBridge.map(Key.NUMPAD_6, Keyboard.KEY_NUMPAD6);
		LWJGL2WindowBridge.map(Key.NUMPAD_7, Keyboard.KEY_NUMPAD7);
		LWJGL2WindowBridge.map(Key.NUMPAD_8, Keyboard.KEY_NUMPAD8);
		LWJGL2WindowBridge.map(Key.NUMPAD_9, Keyboard.KEY_NUMPAD9);
		LWJGL2WindowBridge.map(Key.NUMPAD_DECIMAL, Keyboard.KEY_DECIMAL);
		LWJGL2WindowBridge.map(Key.NUMPAD_DIVIDE, Keyboard.KEY_DIVIDE);
		LWJGL2WindowBridge.map(Key.NUMPAD_MULTIPLY, Keyboard.KEY_MULTIPLY);
		LWJGL2WindowBridge.map(Key.NUMPAD_SUBTRACT, Keyboard.KEY_SUBTRACT);
		LWJGL2WindowBridge.map(Key.NUMPAD_ADD, Keyboard.KEY_ADD);
		LWJGL2WindowBridge.map(Key.NUMPAD_ENTER, Keyboard.KEY_NUMPADENTER);
		LWJGL2WindowBridge.map(Key.NUMPAD_EQUAL, Keyboard.KEY_NUMPADEQUALS);
		LWJGL2WindowBridge.map(Key.LEFT_SHIFT, Keyboard.KEY_LSHIFT);
		LWJGL2WindowBridge.map(Key.LEFT_CONTROL, Keyboard.KEY_LCONTROL);
		LWJGL2WindowBridge.map(Key.LEFT_ALT, Keyboard.KEY_LMENU);
		LWJGL2WindowBridge.map(Key.LEFT_SUPER, Keyboard.KEY_LMETA);
		LWJGL2WindowBridge.map(Key.RIGHT_SHIFT, Keyboard.KEY_RSHIFT);
		LWJGL2WindowBridge.map(Key.RIGHT_CONTROL, Keyboard.KEY_RCONTROL);
		LWJGL2WindowBridge.map(Key.RIGHT_ALT, Keyboard.KEY_RMENU);
		LWJGL2WindowBridge.map(Key.RIGHT_SUPER, Keyboard.KEY_RMETA);
		LWJGL2WindowBridge.map(Key.MENU, Keyboard.KEY_APPS);
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
	public boolean isMouseGrabbed() {
		return Mouse.isGrabbed();
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		final Integer code = LWJGL2WindowBridge.CODE_MAP.get(key);
		return code != null && Keyboard.isKeyDown(code);
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

	@Override
	public void setClipboard(final @NonNull String text) {
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
		} catch (final Exception silent) {}
	}

	public static @NonNull Key getKey(final int code) {
		return code < 0 || code >= LWJGL2WindowBridge.KEY_ARRAY.length ? Key.UNKNOWN : LWJGL2WindowBridge.KEY_ARRAY[code];
	}

	private static void map(final Key key, final int code) {
		LWJGL2WindowBridge.CODE_MAP.put(key, code);
		LWJGL2WindowBridge.KEY_ARRAY[code] = key;
	}

}