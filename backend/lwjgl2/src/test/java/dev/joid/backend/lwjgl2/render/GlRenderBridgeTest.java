package dev.joid.backend.lwjgl2.render;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.opengl.GL11;

import dev.joid.backend.lwjgl2.snapshot.Lwjgl2SnapshotBackend;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.state.FixedMatrixImport;
import dev.joid.base.opengl.render.vertex.ArrayObjectVertexInput;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.resource.Resource;

public class GlRenderBridgeTest {

	private static Lwjgl2SnapshotBackend backend;

	@BeforeClass
	public static void startBackend() {
		GlRenderBridgeTest.backend = new Lwjgl2SnapshotBackend();
		GlRenderBridgeTest.backend.create(64, 64);
	}

	@AfterClass
	public static void stopBackend() {
		GlRenderBridgeTest.backend.destroy();
	}

	@Test
	public void importsTheFixedMatrices() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0D, 320D, 240D, 0D, 1000D, 3000D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(5F, 7F, -2000F);
		FixedMatrixImport.create(bridge).apply();

		final MatrixStack projection = new MatrixStack();
		projection.ortho(0D, 320D, 240D, 0D, 1000D, 3000D);
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(5D, 7D, -2000D);
		Assert.assertArrayEquals(projection.getMatrix(), bridge.getProjection().getMatrix(), 1E-6F);
		Assert.assertArrayEquals(modelView.getMatrix(), bridge.getModelView().getMatrix(), 0F);
	}

	@Test
	public void usesItsOwnVertexArrayWhenTheContextHasOne() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		Assume.assumeTrue(bridge.getCapabilities().hasVertexArrays());
		Assert.assertTrue(bridge.getVertexInput() instanceof ArrayObjectVertexInput);
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