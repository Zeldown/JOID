package dev.joid.lib.render.framebuffer;

import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;
import dev.joid.lib.bridge.clock.RewoundClockBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;

public class FrameBufferPoolTest {

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@Test
	public void givesAsManyFrameBuffersAsAskedToTheSameOwner() {
		final Object owner = new Object();
		Assert.assertEquals(1, FrameBufferPool.allocate(owner, 0, 1, 64, 64).length);
		Assert.assertEquals(2, FrameBufferPool.allocate(owner, 0, 2, 64, 64).length);
		Assert.assertEquals(1, FrameBufferPool.allocate(owner, 0, 1, 32, 32).length);
	}

	@Test
	public void givesNewFrameBuffersAfterAFailedGrowth() {
		final Object owner = new Object();
		final IFrameBuffer[] small = FrameBufferPool.allocate(owner, 0, 1, 10, 10);
		final IRenderBridge failing = (IRenderBridge) Proxy.newProxyInstance(FrameBufferPoolTest.class.getClassLoader(), new Class<?>[] {IRenderBridge.class}, (proxy, method, arguments) -> {
			switch (method.getName()) {
			case "hashCode":
				return System.identityHashCode(proxy);
			case "equals":
				return proxy == arguments[0];
			default:
				throw new IllegalStateException(method.getName());
			}
		});
		BridgeHandler.RENDER.register(failing, 1);
		try {
			Assert.assertThrows(IllegalStateException.class, () -> FrameBufferPool.allocate(owner, 0, 1, 100, 100));
		} finally {
			BridgeHandler.RENDER.unregister(failing);
		}

		final IFrameBuffer[] again = FrameBufferPool.allocate(owner, 0, 1, 10, 10);
		Assert.assertNotSame(small, again);
		Assert.assertFalse(((RecordingFrameBuffer) again[0]).isDeleted());
	}

	@Test
	public void shrinksToItsPeakOfTheLastFiveSeconds() {
		final ManualClockBridge clock = new ManualClockBridge(1000L);
		final RewoundClockBridge wall = new RewoundClockBridge(3_600_000L, clock);
		BridgeHandler.CLOCK.register(clock);
		try {
			final Object owner = new Object();
			FrameBufferPool.allocate(owner, 0, 1, 200, 100);
			final IFrameBuffer[] large = FrameBufferPool.allocate(owner, 0, 1, 4000, 4000);
			Assert.assertEquals(4096, large[0].getWidth());
			BridgeHandler.CLOCK.register(wall);
			for (int i = 0; i < 9; i++) {
				clock.advance(1000L);
				FrameBufferPool.releaseUnused();
				Assert.assertSame(large, FrameBufferPool.allocate(owner, 0, 1, 200, 100));
			}
			clock.advance(1000L);
			FrameBufferPool.releaseUnused();
			Assert.assertTrue(((RecordingFrameBuffer) large[0]).isDeleted());
			final IFrameBuffer[] small = FrameBufferPool.allocate(owner, 0, 1, 200, 100);
			Assert.assertEquals(256, small[0].getWidth());
			Assert.assertEquals(128, small[0].getHeight());
		} finally {
			BridgeHandler.CLOCK.unregister(wall);
			BridgeHandler.CLOCK.unregister(clock);
		}
	}

}