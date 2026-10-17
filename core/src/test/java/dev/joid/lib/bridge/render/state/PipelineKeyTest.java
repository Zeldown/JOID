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
		final RenderState first = RenderState.create();
		final RenderState second = RenderState.create();
		first.blend(BlendState.of(BlendState.Equation.ADD, BlendState.Factor.ONE, BlendState.Factor.ONE_MINUS_SRC_ALPHA));
		second.blend(BlendState.PREMULTIPLIED);
		Assert.assertEquals(PipelineKey.of(PipelineKeyTest.SHADER, first, Primitive.TRIANGLES), PipelineKey.of(PipelineKeyTest.SHADER, second, Primitive.TRIANGLES));
		Assert.assertEquals(PipelineKey.of(PipelineKeyTest.SHADER, first, Primitive.TRIANGLES).hashCode(), PipelineKey.of(PipelineKeyTest.SHADER, second, Primitive.TRIANGLES).hashCode());
	}

	@Test
	public void differsFromAKeyOfAnotherBlend() {
		final RenderState first = RenderState.create();
		final RenderState second = RenderState.create();
		first.blend(BlendState.NORMAL);
		second.blend(BlendState.PREMULTIPLIED);
		Assert.assertNotEquals(PipelineKey.of(PipelineKeyTest.SHADER, first, Primitive.TRIANGLES), PipelineKey.of(PipelineKeyTest.SHADER, second, Primitive.TRIANGLES));
	}

	@Test
	public void differsFromAKeyOfAnotherShader() {
		final RenderState state = RenderState.create();
		Assert.assertNotEquals(PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES), PipelineKey.of(new RecordingShader(), state, Primitive.TRIANGLES));
	}

	@Test
	public void ignoresTheFactorsOfADisabledBlend() {
		final RenderState state = RenderState.create();
		state.blend(BlendState.DISABLED);
		Assert.assertSame(BlendState.DISABLED, PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES).getBlend());
	}

	@Test
	public void writesNoDepthWithoutTheDepthTest() {
		final RenderState state = RenderState.create();
		state.depthTest(false);
		state.depthWrite(true);
		Assert.assertFalse(PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES).isDepthWrite());
		state.depthTest(true);
		Assert.assertTrue(PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES).isDepthWrite());
	}

	@Test
	public void separatesTrianglesFromLines() {
		final RenderState state = RenderState.create();
		Assert.assertNotEquals(PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES), PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.LINES));
	}

	@Test
	public void writesTheStencilWithoutBlendNorDepth() {
		final RenderState state = RenderState.create();
		state.blend(BlendState.NORMAL);
		state.colorWrite(false);
		state.depthTest(true);
		state.cull(true);
		final PipelineKey key = PipelineKey.stencil(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES);
		Assert.assertTrue(key.isStencil());
		Assert.assertSame(BlendState.DISABLED, key.getBlend());
		Assert.assertTrue(key.isColorWrite());
		Assert.assertFalse(key.isDepthTest());
		Assert.assertFalse(key.isDepthWrite());
		Assert.assertTrue(key.isCull());
		Assert.assertNotEquals(PipelineKey.of(PipelineKeyTest.SHADER, state, Primitive.TRIANGLES), key);
	}

}