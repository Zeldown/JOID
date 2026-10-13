package dev.joid.backend.lwjgl2.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.input.Keyboard;

import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.ConsumableEvent;
import lombok.NonNull;

public class Lwjgl2InputForwarderTest {

	private final List<String>  trace  = new ArrayList<>();
	private final TraceUI       ui     = new TraceUI(this.trace);
	private final DemoUIBridge  bridge = new DemoUIBridge();
	private final IWindowBridge window = new FixedWindowBridge();

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
	public void forwardsTheMouseButtons() {
		this.input.mousePressed(1);
		this.input.mouseMoved();
		this.input.mouseReleased(1);
		Assert.assertEquals(Arrays.asList("pressed RIGHT", "dragged RIGHT", "released RIGHT"), this.trace);
	}

	@Test
	public void forwardsTheWheelInNotches() {
		this.input.mouseScrolled(-240);
		Assert.assertEquals(Collections.singletonList("scrolled -2.0"), this.trace);
	}

	@Test
	public void forwardsTheKeyAndTheCharacterApart() {
		this.input.keyPressed(Keyboard.KEY_A);
		this.input.charTyped('a');
		Assert.assertEquals(Arrays.asList("key A", "char a"), this.trace);
	}

	@Test
	public void forwardsNoControlCharacter() {
		Assert.assertFalse(this.input.charTyped('\u0003'));
		Assert.assertFalse(this.input.charTyped('\r'));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void reportsTheEventsTheUiConsumed() {
		Assert.assertFalse(this.input.keyPressed(Keyboard.KEY_A));
		Assert.assertFalse(this.input.keyReleased(Keyboard.KEY_A));
		Assert.assertFalse(this.input.charTyped('a'));
		this.ui.cancel = true;
		Assert.assertTrue(this.input.keyPressed(Keyboard.KEY_A));
		Assert.assertTrue(this.input.keyReleased(Keyboard.KEY_A));
		Assert.assertTrue(this.input.charTyped('a'));
		Assert.assertTrue(this.input.mousePressed(0));
		Assert.assertTrue(this.input.mouseMoved());
		Assert.assertTrue(this.input.mouseReleased(0));
		Assert.assertTrue(this.input.mouseScrolled(120));
	}

	public static final class TraceUI extends UI {

		private final List<String> trace;

		private boolean cancel;

		public TraceUI(final List<String> trace) {
			this.trace = trace;
			super.handleMousePressed(event -> this.record("pressed " + event.getButton(), event));
			super.handleMouseDragged(event -> this.record("dragged " + event.getButton(), event));
			super.handleMouseReleased(event -> this.record("released " + event.getButton(), event));
			super.handleMouseScroll(event -> this.record("scrolled " + event.getNotchesY(), event));
			super.handleKeyPressed(event -> this.record("key " + event.getKey(), event));
			super.handleCharTyped(event -> this.record("char " + new String(Character.toChars(event.getCodepoint())), event));
		}

		private void record(final String event, final ConsumableEvent input) {
			this.trace.add(event);
			if (this.cancel) {
				input.consume();
			}
		}

	}

	public static final class FixedWindowBridge implements IWindowBridge {

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
			return false;
		}

		@Override
		public @NonNull String getClipboard() {
			return "";
		}

		@Override
		public void setClipboard(final @NonNull String text) {}

	}

}