package dev.joid.backend.lwjgl3.render;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.base.opengl.capability.GlProfile;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.state.FixedMatrixImport;
import dev.joid.internal.JOID;
import dev.joid.lib.resource.Resource;

public class GlRenderBridgeTest {

	private static Lwjgl3SnapshotBackend backend;

	@BeforeClass
	public static void startBackend() {
		GlRenderBridgeTest.backend = new Lwjgl3SnapshotBackend();
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
		Assert.assertEquals(bridge.getCapabilities().hasVertexArrays(), bridge.getStrategies().isVertexArrayObject());
	}

	@Test
	public void refusesToImportMatricesFromACoreContext() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		Assume.assumeTrue(bridge.getCapabilities().getProfile() != GlProfile.COMPATIBILITY);
		try {
			FixedMatrixImport.create(bridge);
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertTrue(e.getMessage().startsWith("Only a compatibility context has fixed-function matrices to import"));
		}
	}

	@Test
	public void failsABorrowedTextureThatWasNeverCreated() {
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final boolean devMode = JOID.inst().isDevMode();
		System.setErr(new PrintStream(output, true));
		JOID.inst().setDevMode(true);
		try {
			final Resource resource = Resource.of(2147483000);
			resource.prepareBind();
			Assert.assertTrue(resource.isFailed());
		} finally {
			JOID.inst().setDevMode(devMode);
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains("[JOID] The resource gl_texture_2147483000 cannot be read and is drawn empty: The borrowed texture 2147483000 is not a texture of the host"));
	}

}