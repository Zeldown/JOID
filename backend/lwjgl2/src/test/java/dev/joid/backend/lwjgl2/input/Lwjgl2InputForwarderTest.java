package dev.joid.backend.lwjgl2.input;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public class Lwjgl2InputForwarderTest {

	private final List<String>      trace   = new ArrayList<>();
	private final TraceUI           ui      = new TraceUI(this.trace);
	private final Set<Integer>      pressed = new LinkedHashSet<>();
	private final DemoUIBridge      bridge  = new DemoUIBridge() {};
	private final FixedWindowBridge window  = new FixedWindowBridge();

	private boolean devMode;

	@Before
	public void openAUi() throws Exception {
		this.devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		BridgeHandler.WINDOW.register(this.window);
		this.bridge.add(this.ui);
		BridgeHandler.UI.register(this.bridge);
		Lwjgl2InputForwarderTest.set(Mouse.class, "created", true);
		Lwjgl2InputForwarderTest.set(Mouse.class, "readBuffer", ByteBuffer.allocate(0));
		Lwjgl2InputForwarderTest.set(Keyboard.class, "created", true);
		Lwjgl2InputForwarderTest.set(Keyboard.class, "repeat_enabled", true);
	}

	@After
	public void closeTheUi() throws Exception {
		for (final int code : new ArrayList<>(this.pressed)) {
			this.poll(code, false, '\0', false);
		}
		Lwjgl2InputForwarderTest.set(Mouse.class, "created", false);
		Lwjgl2InputForwarderTest.set(Keyboard.class, "created", false);
		this.bridge.closeAll();
		BridgeHandler.UI.unregister(this.bridge);
		BridgeHandler.WINDOW.unregister(this.window);
		JOIDEnv.devMode(this.devMode);
	}

	@Test
	public void namesTheLettersAndPunctuationAfterTheirCharacter() throws Exception {
		this.keyPressed(Keyboard.KEY_Q, 'a');
		this.keyPressed(Keyboard.KEY_SEMICOLON, 'M');
		this.keyPressed(Keyboard.KEY_W, '\u001A');
		this.keyPressed(Keyboard.KEY_M, ',');
		this.keyPressed(Keyboard.KEY_COMMA, ';');
		Assert.assertEquals(Arrays.asList("key A", "key M", "key Z", "key COMMA", "key SEMICOLON"), this.trace);
	}

	@Test
	public void keepsTheKeyTypedWithoutModifierWhileAModifierIsHeld() throws Exception {
		this.keyPressed(Keyboard.KEY_M, ',');
		this.window.keys.add(Key.LEFT_SHIFT);
		this.keyPressed(Keyboard.KEY_LSHIFT, '\0');
		this.keyPressed(Keyboard.KEY_M, '?');
		this.keyPressed(Keyboard.KEY_PERIOD, '/');
		Assert.assertEquals(Arrays.asList("key COMMA", "key LEFT_SHIFT", "key COMMA", "key PERIOD"), this.trace);
	}

	@Test
	public void keepsThePositionOfTheDigits() throws Exception {
		this.keyPressed(Keyboard.KEY_1, '&');
		this.keyPressed(Keyboard.KEY_2, 'é');
		Assert.assertEquals(Arrays.asList("key DIGIT_1", "key DIGIT_2"), this.trace);
	}

	@Test
	public void keepsThePositionOfTheKeysOutsideTheMainBlock() throws Exception {
		this.keyPressed(Keyboard.KEY_BACK, '\b');
		this.keyPressed(Keyboard.KEY_RETURN, '\r');
		this.keyPressed(Keyboard.KEY_NUMPAD1, '1');
		Assert.assertEquals(Arrays.asList("key BACKSPACE", "key ENTER", "key " + Lwjgl2Keys.getPhysicalKey(Keyboard.KEY_NUMPAD1)), this.trace);
	}

	@Test
	public void typesTheRepeatedCharacterWithoutPressingTheKeyAgain() throws Exception {
		this.ui.handleCharTyped(event -> this.trace.add("char " + (char) event.getCodepoint()));
		this.keyPressed(Keyboard.KEY_Q, 'a');
		this.keyPressed(Keyboard.KEY_Q, 'a', true);
		Assert.assertEquals(Arrays.asList("key A", "char a", "char a"), this.trace);
	}

	@Test
	public void releasesTheKeyItPressed() throws Exception {
		this.window.keys.add(Key.Q);
		this.keyPressed(Keyboard.KEY_Q, 'a');
		Assert.assertTrue(Key.A.isDown());
		Assert.assertTrue(Key.Q.isPhysicalDown());

		this.poll(Keyboard.KEY_Q, false, '\0', false);
		Assert.assertFalse(Key.A.isDown());
	}

	private void keyPressed(final int code, final char character) throws Exception {
		this.keyPressed(code, character, false);
	}

	private void keyPressed(final int code, final char character, final boolean repeat) throws Exception {
		this.poll(code, true, character, repeat);
	}

	private void poll(final int code, final boolean state, final char character, final boolean repeat) throws Exception {
		if (state) {
			this.pressed.add(code);
		} else {
			this.pressed.remove(code);
		}
		final ByteBuffer events = ByteBuffer.allocate(Keyboard.EVENT_SIZE);
		events.putInt(code).put((byte) (state ? 1 : 0)).putInt(character).putLong(0L).put((byte) (repeat ? 1 : 0)).flip();
		Lwjgl2InputForwarderTest.set(Keyboard.class, "readBuffer", events);
		Lwjgl2InputForwarder.poll();
	}

	private static void set(final Class<?> type, final String name, final Object value) throws Exception {
		final Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		field.set(null, value);
	}

	public static final class TraceUI extends UI {

		private final List<String> trace;

		public TraceUI(final List<String> trace) {
			this.trace = trace;
			super.handleKeyPressed(event -> this.trace.add("key " + event.getKey()));
		}

	}

	public static final class FixedWindowBridge implements IWindowBridge {

		private final Set<Key> keys = EnumSet.noneOf(Key.class);

		@Override
		public int getWidth() {
			return 1920;
		}

		@Override
		public int getHeight() {
			return 1080;
		}

		@Override
		public double getMouseX() {
			return 0D;
		}

		@Override
		public double getMouseY() {
			return 0D;
		}

		@Override
		public boolean isMouseGrabbed() {
			return false;
		}

		@Override
		public boolean isKeyDown(final @NonNull Key key) {
			return this.keys.contains(key);
		}

		@Override
		public @NonNull String getClipboard() {
			return "";
		}

		@Override
		public void setClipboard(final @NonNull String text) {}

	}

}