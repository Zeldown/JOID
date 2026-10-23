package dev.joid.backend.lwjgl3.render;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import dev.joid.base.opengl.capability.GlProfile;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.framebuffer.GlFrameBuffer;
import dev.joid.base.opengl.render.state.FixedMatrixImport;
import dev.joid.base.opengl.render.texture.BlitGlMipmapBuilder;
import dev.joid.base.opengl.render.texture.GlBorrowedTexture;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
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
			Assert.assertEquals("A texture of " + (maxSize + 1) + "x1 exceeds the maximum size " + maxSize + " of " + bridge.getCapabilities().getName() + ", downscale the image", e.getMessage());
		}
	}

	@Test
	public void choosesTheStrategiesOfTheContextOnce() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		Assert.assertSame(bridge.getFrameBufferBinding(), bridge.getBinding().getFrameBufferBinding(bridge.getCapabilities().getFrameBufferFamily()));
		Assert.assertEquals(bridge.getCapabilities().hasVertexArrays(), bridge.getVertexInput().getArray() != 0);
		Assert.assertEquals(bridge.getCapabilities().hasFrameBufferBlit(), bridge.getMipmapBuilder() instanceof BlitGlMipmapBuilder);
	}

	@Test
	public void restoresTheReadFrameBufferOfTheHostAfterARaster() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		Assume.assumeTrue(bridge.getCapabilities().hasFrameBufferBlit());
		final int read = GL30C.glGenFramebuffers();
		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, read);
		bridge.beginFrame();
		try {
			final IFrameBuffer target = bridge.createFrameBuffer(8, 8);
			bridge.raster(target, 8, 8, () -> {});
			target.delete();
		} finally {
			bridge.endFrame();
		}

		final int bound = GL11C.glGetInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING);
		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
		GL30C.glDeleteFramebuffers(read);
		Assert.assertEquals(read, bound);
	}

	@Test
	public void drawsTheScreenIntoTheFrameBufferThatTheHostBound() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		Assume.assumeTrue(bridge.getCapabilities().getFrameBufferFamily() == GlFrameBufferFamily.CORE);
		final GlFrameBuffer host = (GlFrameBuffer) bridge.createFrameBuffer(8, 8);
		final ByteBuffer pixel = BufferUtils.createByteBuffer(4);
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, host.getId());
		bridge.beginFrame();
		try {
			bridge.screen(8, 8);
			bridge.clearColor(1F, 0F, 0F, 1F);
		} finally {
			bridge.endFrame();
		}

		final int bound = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
		GL11C.glReadPixels(0, 0, 1, 1, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, pixel);
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
		host.delete();
		Assert.assertEquals(host.getId(), bound);
		Assert.assertEquals(255, pixel.get(0) & 0xFF);
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
		final boolean devMode = JOIDEnv.isDevMode();
		final Resource resource = Resource.of(GlBorrowedTexture.of(2147483000));
		System.setErr(new PrintStream(output, true));
		JOIDEnv.devMode(true);
		try {
			resource.prepareBind();
			Assert.assertTrue(resource.isFailed());
		} finally {
			resource.clear();
			JOIDEnv.devMode(devMode);
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains(" cannot be read: The borrowed texture 2147483000 is not a texture of the host, borrow a texture that the host created and has not deleted. It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production."));
	}

}