package dev.joid.lib.bridge.clock;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;

public class SystemClockBridgeTest {

	@Test
	public void readsTheNanoTimeOfTheSystem() {
		final long before = System.nanoTime();
		final long time = SystemClockBridge.create().nanoTime();
		Assert.assertTrue(time >= before);
		Assert.assertTrue(time <= System.nanoTime());
	}

	@Test
	public void readsTheWallClockOfTheSystem() {
		final long before = System.currentTimeMillis();
		final long time = SystemClockBridge.create().currentTimeMillis();
		Assert.assertTrue(time >= before);
		Assert.assertTrue(time <= System.currentTimeMillis());
	}

	@Test
	public void isRegisteredByDefault() {
		Assert.assertNotNull(BridgeHandler.CLOCK.getBridge(SystemClockBridge.class));
	}

}