package dev.joid.base.lwjgl2.window;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.base.lwjgl2.input.Lwjgl2InputForwarder;
import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;

public class Lwjgl2WindowBridgeEventTest {

	private final List<Double>       trace       = new ArrayList<>();
	private final DemoUIBridge       bridge      = new DemoUIBridge() {};
	private final Lwjgl2WindowBridge window      = new Lwjgl2WindowBridge();
	private final Map<Field, Object> previousMap = new LinkedHashMap<>();

	private boolean devMode;

	@Before
	public void openAUi() throws Exception {
		Lwjgl2Natives.install();
		this.devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		BridgeHandler.WINDOW.register(this.window);
		this.bridge.add(new TraceUI(this.trace));
		BridgeHandler.UI.register(this.bridge);
		this.replace("created", true);
		this.replace("isGrabbed", false);
		this.replace("clipMouseCoordinatesToWindow", false);
		this.replace("x", 300);
		this.replace("y", 200);
	}

	@After
	public void closeTheUi() throws Exception {
		for (final Map.Entry<Field, Object> previous : this.previousMap.entrySet()) {
			previous.getKey().set(null, previous.getValue());
		}
		this.bridge.closeAll();
		BridgeHandler.UI.unregister(this.bridge);
		BridgeHandler.WINDOW.unregister(this.window);
		JOIDEnv.devMode(this.devMode);
	}

	@Test
	public void readsThePositionOfTheEventWhileTheForwarderForwardsIt() throws Exception {
		final ByteBuffer events = ByteBuffer.allocate(Mouse.EVENT_SIZE);
		events.put((byte) 0).put((byte) 1).putInt(40).putInt(50).putInt(0).putLong(0L).flip();
		this.replace("readBuffer", events);
		Assert.assertTrue(Mouse.next());
		Lwjgl2InputForwarder.forwardMouseEvent();
		final double scale = Display.getPixelScaleFactor();
		Assert.assertEquals(Arrays.asList(40D * scale, (Display.getHeight() - 1 - 50) * scale), this.trace);
		Assert.assertEquals(300D * scale, this.window.getMouseX(), 0D);
		Assert.assertEquals((Display.getHeight() - 1 - 200) * scale, this.window.getMouseY(), 0D);
	}

	private void replace(final String name, final Object value) throws Exception {
		final Field field = Mouse.class.getDeclaredField(name);
		field.setAccessible(true);
		this.previousMap.putIfAbsent(field, field.get(null));
		field.set(null, value);
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class TraceUI extends UI {

		public TraceUI(final List<Double> trace) {
			super.handleMousePressed(event -> trace.addAll(Arrays.asList(BridgeHandler.WINDOW.get().getMouseX(), BridgeHandler.WINDOW.get().getMouseY())));
		}

	}

}