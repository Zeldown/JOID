package dev.joid.lib.bridge.render.state;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.vertex.Primitive;

public class PipelineKeyTest {

	private static final IShader SHADER = new RecordingShader();

	@Test
	public void equalsAKeyOfAnEqualBlend() {
		final RenderState first = new RenderState();
		final RenderState second = new RenderState();
		first.setBlend(BlendState.create(BlendState.Equation.ADD, BlendState.Factor.ONE, BlendState.Factor.ONE_MINUS_SRC_ALPHA));
		second.setBlend(BlendState.PREMULTIPLIED);
		Assert.assertEquals(PipelineKey.create(PipelineKeyTest.SHADER, first, Primitive.TRIANGLES), PipelineKey.create(PipelineKeyTest.SHADER, second, Primitive.TRIANGLES));
		Assert.assertEquals(PipelineKey.create(PipelineKeyTest.SHADER, first, Primitive.TRIANGLES).hashCode(), PipelineKey.create(PipelineKeyTest.SHADER, second, Primitive.TRIANGLES).hashCode());
	}

	@Test
	public void differsFromAKeyOfAnotherBlend() {
		final RenderState first = new RenderState();
		final RenderState second = new RenderState();
		first.setBlend(BlendState.NORMAL);
		second.setBlend(BlendState.PREMULTIPLIED);
		Assert.assertNotEquals(PipelineKey.create(PipelineKeyTest.SHADER, first, Primitive.TRIANGLES), PipelineKey.create(PipelineKeyTest.SHADER, second, Primitive.TRIANGLES));
	}

	@Test
	public void differsFromAKeyOfAnotherShader() {
		final RenderState state = new RenderState();
		Assert.assertNotEquals(PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES), PipelineKey.create(new RecordingShader(), state, Primitive.TRIANGLES));
	}

	@Test
	public void ignoresTheFactorsOfADisabledBlend() {
		final RenderState state = new RenderState();
		state.setBlend(BlendState.DISABLED);
		Assert.assertSame(BlendState.DISABLED, PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES).getBlend());
	}

	@Test
	public void writesNoDepthWithoutTheDepthTest() {
		final RenderState state = new RenderState();
		state.setDepthTest(false);
		state.setDepthWrite(true);
		Assert.assertFalse(PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES).isDepthWrite());
		state.setDepthTest(true);
		Assert.assertTrue(PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES).isDepthWrite());
	}

	@Test
	public void separatesTrianglesFromLines() {
		final RenderState state = new RenderState();
		Assert.assertNotEquals(PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES), PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.LINES));
	}

	@Test
	public void writesTheStencilWithoutBlendNorDepth() {
		final RenderState state = new RenderState();
		state.setBlend(BlendState.NORMAL);
		state.setColorWrite(false);
		state.setDepthTest(true);
		state.setCull(true);
		final PipelineKey key = PipelineKey.stencil(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES);
		Assert.assertTrue(key.isStencil());
		Assert.assertSame(BlendState.DISABLED, key.getBlend());
		Assert.assertTrue(key.isColorWrite());
		Assert.assertFalse(key.isDepthTest());
		Assert.assertFalse(key.isDepthWrite());
		Assert.assertTrue(key.isCull());
		Assert.assertNotEquals(PipelineKey.create(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES), key);
	}

}