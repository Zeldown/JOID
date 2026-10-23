package dev.joid.backend.lwjgl3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.backend.lwjgl3.snapshot.SnapshotProfile;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.StackUIBridge;
import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.bridge.ui.UIBridgeRegistry;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;

public class Lwjgl3BackendTest {

	@After
	public void forgetTheScreenSize() throws ReflectiveOperationException {
		final Field width = UIBridgeRegistry.class.getDeclaredField("width");
		final Field height = UIBridgeRegistry.class.getDeclaredField("height");
		width.setAccessible(true);
		height.setAccessible(true);
		width.setInt(BridgeHandler.UI, 0);
		height.setInt(BridgeHandler.UI, 0);
	}

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), Lwjgl3Backend.create(0L).getVersion());
	}

	@Test
	public void registersAWindowUIBridgeThatClearsToItsBackground() {
		final List<UIBridge> previous = Lwjgl3BackendTest.unregisterUIBridges();
		final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();
		try {
			backend.create(64, 64);
			final WindowUIBridge bridge = BridgeHandler.UI.getBridge(WindowUIBridge.class);
			Assert.assertNotNull(bridge);
			Assert.assertEquals(Color.BLACK, bridge.getBackground());

			bridge.background(Color.RED);
			BridgeHandler.RENDER.get().screen(64, 64);
			BridgeHandler.UI.frame();
			Assert.assertEquals(0xFFFF0000, backend.capture(64, 64).getPixels()[0]);
		} finally {
			backend.destroy();
			Lwjgl3BackendTest.unregisterUIBridges();
			previous.forEach(BridgeHandler.UI::register);
		}
	}

	@Test
	public void keepsTheUIBridgeRegisteredBefore() {
		final List<UIBridge> previous = Lwjgl3BackendTest.unregisterUIBridges();
		final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();
		final StackUIBridge bridge = new StackUIBridge() {};
		BridgeHandler.UI.register(bridge);
		try {
			backend.create(64, 64);
			Assert.assertNull(BridgeHandler.UI.getBridge(WindowUIBridge.class));
			Assert.assertEquals(Collections.singletonList(bridge), Lwjgl3BackendTest.unregisterUIBridges());
		} finally {
			backend.destroy();
			Lwjgl3BackendTest.unregisterUIBridges();
			previous.forEach(BridgeHandler.UI::register);
		}
	}

	@Test
	public void refusesOpenGl20() {
		Assume.assumeTrue(SnapshotProfile.current() == SnapshotProfile.GL_20);
		final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();
		try {
			backend.create(64, 64);
			Assert.fail("JOID accepted an OpenGL 2.0 context");
		} catch (final IllegalStateException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().startsWith("JOID needs OpenGL 2.1 with GLSL 1.20, this context offers OpenGL 2.0"));
		} finally {
			backend.destroy();
		}
	}

	private static List<UIBridge> unregisterUIBridges() {
		final List<UIBridge> bridgeList = new ArrayList<>();
		UIBridge bridge;
		while ((bridge = BridgeHandler.UI.find(current -> true)) != null) {
			BridgeHandler.UI.unregister(bridge);
			bridgeList.add(0, bridge);
		}
		return bridgeList;
	}

}