package dev.joid.base.glfw.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.glfw.GLFW;

import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.callback.DispatchContext;
import lombok.NonNull;

public class GlfwInputForwarderTest {

	private final List<String>  trace  = new ArrayList<>();
	private final TraceUI       ui     = new TraceUI(this.trace);
	private final DemoUIBridge  bridge = new DemoUIBridge();
	private final IWindowBridge window = new FixedWindowBridge();

	private GlfwInputForwarder input;

	@Before
	public void openAUi() {
		BridgeHandler.WINDOW.register(this.window);
		this.bridge.add(this.ui);
		this.input = GlfwInputForwarder.create(this.bridge);
	}

	@After
	public void closeTheUi() {
		this.bridge.closeAll();
		BridgeHandler.WINDOW.unregister(this.window);
	}

	@Test
	public void forwardsTheMouseButtons() {
		this.input.mousePressed(1);
		this.input.mouseReleased(1);
		Assert.assertEquals(Arrays.asList("pressed RIGHT", "released RIGHT"), this.trace);
	}

	@Test
	public void dragsWithThePressedButton() {
		this.input.mousePressed(0);
		this.input.mouseMoved();
		this.input.mouseReleased(0);
		this.input.mouseMoved();
		Assert.assertEquals(Arrays.asList("pressed LEFT", "dragged LEFT", "released LEFT"), this.trace);
	}

	@Test
	public void forwardsTheWheelInNotches() {
		this.input.mouseScrolled(0D, -0.5D);
		Assert.assertEquals(Collections.singletonList("scrolled -0.5"), this.trace);
	}

	@Test
	public void forwardsTheKeyAndTheCharacterAsTheyCome() {
		this.input.keyPressed(GLFW.GLFW_KEY_F3);
		this.input.charTyped(0xE9);
		this.input.keyPressed(GLFW.GLFW_KEY_ENTER);
		Assert.assertEquals(Arrays.asList("key F3", "char é", "key ENTER"), this.trace);
	}

	@Test
	public void forwardsACharacterOutsideTheBasicPlaneInOneCall() {
		this.input.charTyped(0x1F600);
		Assert.assertEquals(Collections.singletonList("char \uD83D\uDE00"), this.trace);
	}

	@Test
	public void reportsTheEventsTheUiConsumed() {
		Assert.assertFalse(this.input.mousePressed(0));
		Assert.assertFalse(this.input.keyPressed(GLFW.GLFW_KEY_F3));
		Assert.assertFalse(this.input.charTyped('a'));
		this.ui.cancel = true;
		Assert.assertTrue(this.input.keyPressed(GLFW.GLFW_KEY_F3));
		Assert.assertTrue(this.input.charTyped('a'));
		Assert.assertTrue(this.input.mousePressed(0));
		Assert.assertTrue(this.input.mouseMoved());
		Assert.assertTrue(this.input.mouseReleased(0));
		Assert.assertTrue(this.input.mouseScrolled(0D, 1D));
	}

	public static final class TraceUI extends UI {

		private final List<String> trace;

		private boolean cancel;

		public TraceUI(final List<String> trace) {
			this.trace = trace;
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
			this.record("pressed " + button, context);
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull DispatchContext context) {
			this.record("dragged " + button, context);
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
			this.record("released " + button, context);
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notches, final @NonNull DispatchContext context) {
			this.record("scrolled " + notches, context);
		}

		@Override
		public void keyPressed(final @NonNull Key key, final @NonNull DispatchContext context) {
			this.record("key " + key, context);
		}

		@Override
		public void charTyped(final int codepoint, final @NonNull DispatchContext context) {
			this.record("char " + new String(Character.toChars(codepoint)), context);
		}

		private void record(final String event, final DispatchContext context) {
			this.trace.add(event);
			if (this.cancel) {
				context.cancel();
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