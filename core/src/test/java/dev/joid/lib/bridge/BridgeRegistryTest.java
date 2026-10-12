package dev.joid.lib.bridge;

import java.util.Arrays;

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
		Assert.assertSame(second, registry.getBridge(ManualClockBridge.class));
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
	public void registersADefaultBridgeOnlyIntoAnEmptyRegistry() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge first = ManualClockBridge.create(1L);
		registry.registerDefault(first);
		registry.registerDefault(ManualClockBridge.create(2L));
		Assert.assertSame(first, registry.get());
		Assert.assertNull(registry.find(bridge -> bridge.currentTimeMillis() == 2L));
	}

	@Test
	public void findsTheLatestBridgeMatchingAFilter() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge early = ManualClockBridge.create(1L);
		registry.register(early);
		registry.register(ManualClockBridge.create(2L));
		Assert.assertSame(early, registry.find(bridge -> bridge.currentTimeMillis() == 1L));
		Assert.assertNull(registry.find(bridge -> bridge.currentTimeMillis() == 3L));
	}

	@Test
	public void givesTheHighestPriorityBeforeTheLatestBridge() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge high = ManualClockBridge.create(1L);
		final ManualClockBridge low = ManualClockBridge.create(2L);
		registry.register(high, 5);
		registry.register(low, -5);
		registry.register(ManualClockBridge.create(3L));
		Assert.assertSame(high, registry.get());
		Assert.assertSame(low, registry.getBridgeList().get(0));
	}

	@Test
	public void ordersItsBridgesByPriorityThenByRegistration() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge low = ManualClockBridge.create(1L);
		final ManualClockBridge first = ManualClockBridge.create(2L);
		final ManualClockBridge second = ManualClockBridge.create(3L);
		final ManualClockBridge high = ManualClockBridge.create(4L);
		registry.register(first);
		registry.register(high, 1);
		registry.register(low, -1);
		registry.register(second);
		Assert.assertEquals(Arrays.asList(low, first, second, high), registry.getBridgeList());
	}

	@Test
	public void movesABridgeRegisteredAgain() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge first = ManualClockBridge.create(1L);
		final ManualClockBridge second = ManualClockBridge.create(2L);
		registry.register(first);
		registry.register(second);
		registry.register(first);
		Assert.assertEquals(Arrays.asList(second, first), registry.getBridgeList());
		registry.register(first, -1);
		Assert.assertEquals(Arrays.asList(first, second), registry.getBridgeList());
	}

	@Test
	public void keepsTheDefaultBridgeBelowEveryOther() {
		final BridgeRegistry<ManualClockBridge> registry = BridgeRegistry.create("Clock");
		final ManualClockBridge fallback = ManualClockBridge.create(1L);
		final ManualClockBridge low = ManualClockBridge.create(2L);
		registry.registerDefault(fallback);
		registry.register(low, Integer.MIN_VALUE + 1);
		Assert.assertSame(low, registry.get());
		Assert.assertEquals(Arrays.asList(fallback, low), registry.getBridgeList());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void givesAReadOnlyListOfItsBridges() {
		BridgeRegistry.<ManualClockBridge>create("Clock").getBridgeList().add(ManualClockBridge.create(1L));
	}

}