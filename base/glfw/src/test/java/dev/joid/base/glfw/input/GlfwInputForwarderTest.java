package dev.joid.base.glfw.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.ConsumableEvent;
import lombok.NonNull;

public class GlfwInputForwarderTest {

	private static long window;

	private final List<String>  trace  = new ArrayList<>();
	private final TraceUI       ui     = new TraceUI(this.trace);
	private final DemoUIBridge  bridge = new DemoUIBridge();
	private final IWindowBridge fixed  = new FixedWindowBridge();

	private boolean devMode;

	@BeforeClass
	public static void createHiddenWindow() {
		Assume.assumeTrue(GLFW.glfwInit());
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GlfwInputForwarderTest.window = GLFW.glfwCreateWindow(64, 64, "JOID input test", 0L, 0L);
		Assume.assumeTrue(GlfwInputForwarderTest.window != 0L);
	}

	@AfterClass
	public static void destroyHiddenWindow() {
		if (GlfwInputForwarderTest.window != 0L) {
			GLFW.glfwDestroyWindow(GlfwInputForwarderTest.window);
		}
		GLFW.glfwTerminate();
	}

	@Before
	public void openAUi() {
		this.devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		BridgeHandler.WINDOW.register(this.fixed);
		this.bridge.add(this.ui);
		BridgeHandler.UI.register(this.bridge);
		GLFW.glfwSetCharCallback(GlfwInputForwarderTest.window, (handle, codepoint) -> this.trace.add("passed char"));
		GLFW.glfwSetCursorPosCallback(GlfwInputForwarderTest.window, (handle, x, y) -> this.trace.add("passed cursor"));
		GLFW.glfwSetScrollCallback(GlfwInputForwarderTest.window, (handle, x, y) -> this.trace.add("passed scroll"));
		GLFW.glfwSetKeyCallback(GlfwInputForwarderTest.window, (handle, code, scancode, action, mods) -> this.trace.add("passed key"));
		GLFW.glfwSetMouseButtonCallback(GlfwInputForwarderTest.window, (handle, code, action, mods) -> this.trace.add("passed button"));
		GlfwInputForwarder.create().attach(GlfwInputForwarderTest.window);
	}

	@After
	public void closeTheUi() {
		Callbacks.glfwFreeCallbacks(GlfwInputForwarderTest.window);
		this.bridge.closeAll();
		BridgeHandler.UI.unregister(this.bridge);
		BridgeHandler.WINDOW.unregister(this.fixed);
		JOIDEnv.devMode(this.devMode);
	}

	@Test
	public void forwardsTheMouseButtons() {
		this.ui.cancel = true;
		GlfwInputForwarderTest.button(1, GLFW.GLFW_PRESS);
		GlfwInputForwarderTest.button(1, GLFW.GLFW_RELEASE);
		Assert.assertEquals(Arrays.asList("pressed RIGHT", "released RIGHT"), this.trace);
	}

	@Test
	public void dragsWithThePressedButton() {
		this.ui.cancel = true;
		GlfwInputForwarderTest.button(0, GLFW.GLFW_PRESS);
		GlfwInputForwarderTest.cursor();
		GlfwInputForwarderTest.button(0, GLFW.GLFW_RELEASE);
		Assert.assertEquals(Arrays.asList("pressed LEFT", "dragged LEFT", "released LEFT"), this.trace);
	}

	@Test
	public void forwardsTheWheelInNotches() {
		this.ui.cancel = true;
		GlfwInputForwarderTest.scroll(0D, -0.5D);
		Assert.assertEquals(Collections.singletonList("scrolled -0.5"), this.trace);
	}

	@Test
	public void forwardsTheKeyAndTheCharacterAsTheyCome() {
		this.ui.cancel = true;
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_F3, GLFW.GLFW_PRESS);
		GlfwInputForwarderTest.character(0xE9);
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_ENTER, GLFW.GLFW_PRESS);
		Assert.assertEquals(Arrays.asList("key F3", "char é", "key ENTER"), this.trace);
	}

	@Test
	public void leavesTheKeyRepeatToTheUi() {
		this.ui.cancel = true;
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_PRESS);
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_REPEAT);
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_REPEAT);
		Assert.assertEquals(Arrays.asList("key BACKSPACE", "passed key", "passed key"), this.trace);
	}

	@Test
	public void forwardsACharacterOutsideTheBasicPlaneInOneCall() {
		this.ui.cancel = true;
		GlfwInputForwarderTest.character(0x1F600);
		Assert.assertEquals(Collections.singletonList("char 😀"), this.trace);
	}

	@Test
	public void passesTheEventsTheUiLeftToThePreviousCallbacks() {
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_F3, GLFW.GLFW_PRESS);
		GlfwInputForwarderTest.key(GLFW.GLFW_KEY_F3, GLFW.GLFW_RELEASE);
		GlfwInputForwarderTest.character('a');
		GlfwInputForwarderTest.button(0, GLFW.GLFW_PRESS);
		GlfwInputForwarderTest.scroll(0D, 1D);
		Assert.assertEquals(Arrays.asList("key F3", "passed key", "passed key", "char a", "passed char", "pressed LEFT", "passed button", "scrolled 1.0", "passed scroll"), this.trace);
	}

	private static void key(final int code, final int action) {
		final GLFWKeyCallback callback = GLFW.glfwSetKeyCallback(GlfwInputForwarderTest.window, null);
		GLFW.glfwSetKeyCallback(GlfwInputForwarderTest.window, callback);
		callback.invoke(GlfwInputForwarderTest.window, code, 0, action, 0);
	}

	private static void character(final int codepoint) {
		final GLFWCharCallback callback = GLFW.glfwSetCharCallback(GlfwInputForwarderTest.window, null);
		GLFW.glfwSetCharCallback(GlfwInputForwarderTest.window, callback);
		callback.invoke(GlfwInputForwarderTest.window, codepoint);
	}

	private static void cursor() {
		final GLFWCursorPosCallback callback = GLFW.glfwSetCursorPosCallback(GlfwInputForwarderTest.window, null);
		GLFW.glfwSetCursorPosCallback(GlfwInputForwarderTest.window, callback);
		callback.invoke(GlfwInputForwarderTest.window, 0D, 0D);
	}

	private static void scroll(final double x, final double y) {
		final GLFWScrollCallback callback = GLFW.glfwSetScrollCallback(GlfwInputForwarderTest.window, null);
		GLFW.glfwSetScrollCallback(GlfwInputForwarderTest.window, callback);
		callback.invoke(GlfwInputForwarderTest.window, x, y);
	}

	private static void button(final int button, final int action) {
		final GLFWMouseButtonCallback callback = GLFW.glfwSetMouseButtonCallback(GlfwInputForwarderTest.window, null);
		GLFW.glfwSetMouseButtonCallback(GlfwInputForwarderTest.window, callback);
		callback.invoke(GlfwInputForwarderTest.window, button, action, 0);
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