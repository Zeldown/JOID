package dev.joid.backend.lwjgl2.render;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.opengl.GL11;

import dev.joid.backend.lwjgl2.snapshot.SnapshotBackend;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.host.HostMatrixImport;
import dev.joid.base.opengl.render.vertex.ArrayObjectVertexInput;
import dev.joid.lib.bridge.render.matrix.MatrixStack;

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
	public void importsTheFixedMatricesOfTheHost() {
		final GlRenderBridge bridge = GlRenderBridgeTest.backend.getBridge();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0D, 320D, 240D, 0D, 1000D, 3000D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(5F, 7F, -2000F);
		HostMatrixImport.create(bridge).apply();

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

}