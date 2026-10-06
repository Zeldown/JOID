package dev.joid.lib.bridge;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.clock.ManualClockBridge;

public class BridgeRegistryTest {

	@Test(expected = IllegalStateException.class)
	public void refusesToGiveABridgeBeforeOneIsRegistered() {
		BridgeRegistry.<ManualClockBridge>create("Clock").get();
	}

	@Test
	public void givesTheLastRegisteredBridge() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge first = ManualClockBridge.create(1L);
		final ManualClockBridge second = ManualClockBridge.create(2L);
		registry.register(first);
		registry.register(second);
		Assert.assertSame(second, registry.get());
		Assert.assertSame(second, registry.getBridge(ManualClockBridge.class).get());
	}

	@Test
	public void fallsBackOnTheBridgeBelowOnceUnregistered() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge first = ManualClockBridge.create(1L);
		final ManualClockBridge second = ManualClockBridge.create(2L);
		registry.register(first);
		registry.register(second);
		registry.unregister(second);
		Assert.assertSame(first, registry.get());
	}

	@Test
	public void findsTheLatestBridgeMatchingAFilter() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge early = ManualClockBridge.create(1L);
		registry.register(early);
		registry.register(ManualClockBridge.create(2L));
		Assert.assertSame(early, registry.find(bridge -> bridge.currentTimeMillis() == 1L).get());
		Assert.assertFalse(registry.find(bridge -> bridge.currentTimeMillis() == 3L).isPresent());
	}

}