package dev.joid.lib.bridge.clock;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;

public class ManualClockBridgeTest {

	@Test
	public void startsItsNanoTimeFromTheClockItReplaces() {
		final long before = BridgeHandler.CLOCK.get().nanoTime();
		final long time = new ManualClockBridge(1735689600000L).nanoTime();
		Assert.assertTrue(time >= before);
		Assert.assertTrue(time <= BridgeHandler.CLOCK.get().nanoTime());
	}

	@Test
	public void movesItsTwoClocksTogether() {
		final ManualClockBridge clock = new ManualClockBridge(1000L);
		final long time = clock.nanoTime();
		clock.advance(25L);
		Assert.assertEquals(1025L, clock.currentTimeMillis());
		Assert.assertEquals(time + 25_000_000L, clock.nanoTime());
		clock.time(500L);
		Assert.assertEquals(500L, clock.currentTimeMillis());
		Assert.assertEquals(time - 500_000_000L, clock.nanoTime());
	}

}