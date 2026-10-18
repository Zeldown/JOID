package dev.joid.lib.render.framebuffer;

import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
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

}