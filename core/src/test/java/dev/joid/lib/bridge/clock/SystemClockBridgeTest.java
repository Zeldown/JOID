package dev.joid.lib.bridge.clock;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;

public class SystemClockBridgeTest {

	@Test
	public void readsTheNanoTimeOfTheSystem() {
		final long before = System.nanoTime();
		final long time = new SystemClockBridge().nanoTime();
		Assert.assertTrue(time >= before);
		Assert.assertTrue(time <= System.nanoTime());
	}

	@Test
	public void readsTheWallClockOfTheSystem() {
		final long before = System.currentTimeMillis();
		final long time = new SystemClockBridge().currentTimeMillis();
		Assert.assertTrue(time >= before);
		Assert.assertTrue(time <= System.currentTimeMillis());
	}

	@Test
	public void isRegisteredByDefault() {
		Assert.assertTrue(BridgeHandler.CLOCK.getBridge(SystemClockBridge.class).isPresent());
		Assert.assertEquals(0, new SystemClockBridge().getIndex());
	}

}