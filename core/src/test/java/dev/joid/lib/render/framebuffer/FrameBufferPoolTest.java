package dev.joid.lib.render.framebuffer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;

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

}