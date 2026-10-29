package dev.joid.base.lwjgl2.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.input.Keyboard;

import dev.joid.lib.input.key.Key;

public class Lwjgl2KeysTest {

	@Test
	public void mapsEachLwjglKeyToTheKeyAtItsPosition() {
		final List<String> keys = new ArrayList<>();
		for (int code = 0; code < Keyboard.KEYBOARD_SIZE; code++) {
			final Key key = Lwjgl2Keys.getPhysicalKey(code);
			if (key != Key.UNKNOWN) {
				keys.add(Keyboard.getKeyName(code) + "=" + key);
			}
		}

		Assert.assertEquals(Arrays.asList(
			"ESCAPE=ESCAPE", "1=DIGIT_1", "2=DIGIT_2", "3=DIGIT_3", "4=DIGIT_4", "5=DIGIT_5", "6=DIGIT_6", "7=DIGIT_7",
			"8=DIGIT_8", "9=DIGIT_9", "0=DIGIT_0", "MINUS=MINUS", "EQUALS=EQUAL", "BACK=BACKSPACE", "TAB=TAB", "Q=Q",
			"W=W", "E=E", "R=R", "T=T", "Y=Y", "U=U", "I=I", "O=O",
			"P=P", "LBRACKET=LEFT_BRACKET", "RBRACKET=RIGHT_BRACKET", "RETURN=ENTER", "LCONTROL=LEFT_CONTROL", "A=A", "S=S", "D=D",
			"F=F", "G=G", "H=H", "J=J", "K=K", "L=L", "SEMICOLON=SEMICOLON", "APOSTROPHE=APOSTROPHE",
			"GRAVE=GRAVE_ACCENT", "LSHIFT=LEFT_SHIFT", "BACKSLASH=BACKSLASH", "Z=Z", "X=X", "C=C", "V=V", "B=B",
			"N=N", "M=M", "COMMA=COMMA", "PERIOD=PERIOD", "SLASH=SLASH", "RSHIFT=RIGHT_SHIFT", "MULTIPLY=NUMPAD_MULTIPLY", "LMENU=LEFT_ALT",
			"SPACE=SPACE", "CAPITAL=CAPS_LOCK", "F1=F1", "F2=F2", "F3=F3", "F4=F4", "F5=F5", "F6=F6",
			"F7=F7", "F8=F8", "F9=F9", "F10=F10", "NUMLOCK=NUM_LOCK", "SCROLL=SCROLL_LOCK", "NUMPAD7=NUMPAD_7", "NUMPAD8=NUMPAD_8",
			"NUMPAD9=NUMPAD_9", "SUBTRACT=NUMPAD_SUBTRACT", "NUMPAD4=NUMPAD_4", "NUMPAD5=NUMPAD_5", "NUMPAD6=NUMPAD_6", "ADD=NUMPAD_ADD", "NUMPAD1=NUMPAD_1", "NUMPAD2=NUMPAD_2",
			"NUMPAD3=NUMPAD_3", "NUMPAD0=NUMPAD_0", "DECIMAL=NUMPAD_DECIMAL", "F11=F11", "F12=F12", "F13=F13", "F14=F14", "F15=F15",
			"F16=F16", "F17=F17", "F18=F18", "F19=F19", "NUMPADEQUALS=NUMPAD_EQUAL", "NUMPADENTER=NUMPAD_ENTER", "RCONTROL=RIGHT_CONTROL", "DIVIDE=NUMPAD_DIVIDE",
			"SYSRQ=PRINT_SCREEN", "RMENU=RIGHT_ALT", "PAUSE=PAUSE", "HOME=HOME", "UP=UP", "PRIOR=PAGE_UP", "LEFT=LEFT", "RIGHT=RIGHT",
			"END=END", "DOWN=DOWN", "NEXT=PAGE_DOWN", "INSERT=INSERT", "DELETE=DELETE", "LMETA=LEFT_SUPER", "RMETA=RIGHT_SUPER", "APPS=MENU"
		), keys);
	}

}