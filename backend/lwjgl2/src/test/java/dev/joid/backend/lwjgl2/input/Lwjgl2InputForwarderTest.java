package dev.joid.backend.lwjgl2.input;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.input.Keyboard;

import dev.joid.backend.lwjgl2.window.Lwjgl2WindowBridge;
import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public class Lwjgl2InputForwarderTest {

	private final List<String>      trace  = new ArrayList<>();
	private final TraceUI           ui     = new TraceUI(this.trace);
	private final DemoUIBridge      bridge = new DemoUIBridge();
	private final FixedWindowBridge window = new FixedWindowBridge();

	private Lwjgl2InputForwarder input;

	@Before
	public void openAUi() {
		BridgeHandler.WINDOW.register(this.window);
		this.bridge.add(this.ui);
		BridgeHandler.UI.register(this.bridge);
		this.input = Lwjgl2InputForwarder.create();
	}

	@After
	public void closeTheUi() {
		this.bridge.closeAll();
		BridgeHandler.UI.unregister(this.bridge);
		BridgeHandler.WINDOW.unregister(this.window);
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
		Assert.assertEquals(Arrays.asList("key BACKSPACE", "key ENTER", "key " + Lwjgl2WindowBridge.getKey(Keyboard.KEY_NUMPAD1)), this.trace);
	}

	@Test
	public void releasesTheKeyItPressed() throws Exception {
		this.window.keys.add(Key.Q);
		this.keyPressed(Keyboard.KEY_Q, 'a');
		Assert.assertTrue(Key.A.isDown());
		Assert.assertTrue(Key.Q.isPhysicalDown());

		final Method release = Lwjgl2InputForwarder.class.getDeclaredMethod("keyReleased", int.class);
		release.setAccessible(true);
		release.invoke(this.input, Keyboard.KEY_Q);
		Assert.assertFalse(Key.A.isDown());
	}

	private void keyPressed(final int code, final char character) throws Exception {
		final Method press = Lwjgl2InputForwarder.class.getDeclaredMethod("keyPressed", int.class, char.class);
		press.setAccessible(true);
		press.invoke(this.input, code, character);
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