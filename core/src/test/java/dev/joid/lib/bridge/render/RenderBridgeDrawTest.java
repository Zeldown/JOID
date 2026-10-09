package dev.joid.lib.bridge.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;

public class RenderBridgeDrawTest {

	@Test
	public void drawsWithTheBoundShader() {
		final CapturingRenderBridge render = new CapturingRenderBridge(64, 64);
		final RecordingShader shader = new RecordingShader();
		render.shader(shader);
		render.draw(Primitive.TRIANGLES, RenderBridgeDrawTest.triangle(3));
		Assert.assertSame(shader, render.getLast().getShader());
	}

	@Test
	public void drawsWithTheDefaultShaderWithoutABoundShader() {
		final CapturingRenderBridge render = new CapturingRenderBridge(64, 64);
		render.draw(Primitive.TRIANGLES, RenderBridgeDrawTest.triangle(3));
		final IShader fixed = render.getLast().getShader();
		Assert.assertNotNull(fixed);
		render.draw(Primitive.LINES, RenderBridgeDrawTest.triangle(2));
		Assert.assertSame(fixed, render.getLast().getShader());
		Assert.assertSame(Primitive.LINES, render.getLast().getPrimitive());
	}

	@Test
	public void skipsAnEmptyBuffer() {
		final CapturingRenderBridge render = new CapturingRenderBridge(64, 64);
		render.draw(Primitive.TRIANGLES, RenderBridgeDrawTest.triangle(0));
		Assert.assertTrue(render.getCaptures().isEmpty());
	}

	@Test
	public void skipsAnEmptyViewport() {
		final CapturingRenderBridge render = new CapturingRenderBridge(64, 64);
		render.viewport(0, 0, 0, 64);
		render.draw(Primitive.TRIANGLES, RenderBridgeDrawTest.triangle(3));
		render.viewport(0, 0, 64, 0);
		render.draw(Primitive.TRIANGLES, RenderBridgeDrawTest.triangle(3));
		Assert.assertTrue(render.getCaptures().isEmpty());
	}

	@Test
	public void skipsAShaderThatDidNotCompile() {
		final CapturingRenderBridge render = new CapturingRenderBridge(64, 64);
		final RecordingShader shader = new RecordingShader();
		shader.setActive(false);
		render.shader(shader);
		render.draw(Primitive.TRIANGLES, RenderBridgeDrawTest.triangle(3));
		Assert.assertTrue(render.getCaptures().isEmpty());
	}

	@Test
	public void createsOneOpaqueWhiteTexture() {
		final CapturingRenderBridge render = new CapturingRenderBridge(64, 64);
		final ITexture texture = render.getEmptyTexture();
		Assert.assertSame(texture, render.getEmptyTexture());
		Assert.assertEquals(1, texture.getWidth());
		Assert.assertEquals(1, texture.getHeight());
		Assert.assertArrayEquals(new int[] {0xFFFFFFFF}, ((RecordingTexture) texture).getPixels());
	}

	private static VertexBuffer triangle(final int count) {
		return VertexBuffer.create(ByteBuffer.allocateDirect(Math.max(count, 1) * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder()), count, false, false, false);
	}

}