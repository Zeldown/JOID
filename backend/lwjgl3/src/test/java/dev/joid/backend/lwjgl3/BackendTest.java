package dev.joid.backend.lwjgl3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.bridge.ui.StackUIBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;

public class BackendTest {

	@Test
	public void checksTheVersionOfJOIDItWasBuiltAgainst() throws IOException {
		final String bytecode = new String(Asset.of(Backend.class.getResourceAsStream("Backend.class")).read(), StandardCharsets.ISO_8859_1);
		Assert.assertTrue(bytecode.contains("checkVersion"));
		Assert.assertTrue(bytecode.contains(JOID.VERSION));
	}

	@Test
	public void registersAWindowUIBridgeThatClearsToItsBackground() {
		final List<IUIBridge> previous = BackendTest.unregisterUIBridges();
		final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();
		try {
			backend.create(64, 64);
			final WindowUIBridge bridge = BridgeHandler.UI.getBridge(WindowUIBridge.class);
			Assert.assertNotNull(bridge);
			Assert.assertEquals(Color.BLACK, bridge.getBackground());

			bridge.background(Color.RED).resize(64, 64);
			bridge.frame();
			Assert.assertEquals(0xFFFF0000, backend.capture(64, 64).getPixels()[0]);
		} finally {
			backend.destroy();
			BackendTest.unregisterUIBridges();
			previous.forEach(BridgeHandler.UI::register);
		}
	}

	@Test
	public void keepsTheUIBridgeRegisteredBefore() {
		final List<IUIBridge> previous = BackendTest.unregisterUIBridges();
		final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();
		final StackUIBridge bridge = new StackUIBridge() {};
		BridgeHandler.UI.register(bridge);
		try {
			backend.create(64, 64);
			Assert.assertNull(BridgeHandler.UI.getBridge(WindowUIBridge.class));
			Assert.assertEquals(Collections.singletonList(bridge), BackendTest.unregisterUIBridges());
		} finally {
			backend.destroy();
			BackendTest.unregisterUIBridges();
			previous.forEach(BridgeHandler.UI::register);
		}
	}

	private static List<IUIBridge> unregisterUIBridges() {
		final List<IUIBridge> bridgeList = new ArrayList<>();
		IUIBridge bridge;
		while ((bridge = BridgeHandler.UI.find(current -> true)) != null) {
			BridgeHandler.UI.unregister(bridge);
			bridgeList.add(0, bridge);
		}
		return bridgeList;
	}

}