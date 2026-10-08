package dev.joid.backend.lwjgl3.render;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.backend.lwjgl3.snapshot.SnapshotBackend;
import dev.joid.base.opengl.render.GlRenderBridge;

public class GlRenderBridgeTest {

	private static SnapshotBackend backend;

	@BeforeClass
	public static void startBackend() {
		GlRenderBridgeTest.backend = new SnapshotBackend();
		GlRenderBridgeTest.backend.create(64, 64);
	}

	@AfterClass
	public static void stopBackend() {
		GlRenderBridgeTest.backend.destroy();
	}

	@Test
	public void refusesATextureLargerThanTheContext() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		final int maxSize = bridge.getCapabilities().getMaxTextureSize();
		try {
			bridge.createTexture().allocate(maxSize + 1, 1);
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			Assert.assertEquals("A texture of " + (maxSize + 1) + "x1 exceeds the maximum size " + maxSize + " of " + bridge.getCapabilities().getName(), e.getMessage());
		}
	}

	@Test
	public void choosesTheStrategiesOfTheContextOnce() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		Assert.assertSame(bridge.getStrategies().getDialect(), bridge.getStrategies().createTranslator().getDialect());
		Assert.assertSame(bridge.getFrameBufferBinding(), bridge.getBinding().getFrameBufferBinding(bridge.getStrategies().getFrameBufferFamily()));
		Assert.assertEquals(bridge.getCapabilities().hasVertexArrays(), bridge.getStrategies().isOwnVertexArray());
	}

}