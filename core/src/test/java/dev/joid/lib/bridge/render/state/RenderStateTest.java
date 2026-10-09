package dev.joid.lib.bridge.render.state;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;

public class RenderStateTest {

	@Test
	public void copiesEveryProperty() {
		final RenderState state = RenderStateTest.create();
		RenderStateTest.assertSameProperties(state, state.copy());
	}

	@Test
	public void loadsEveryPropertyIntoTheSameInstance() {
		final RenderState source = RenderStateTest.create();
		final RenderState target = new RenderState();
		Assert.assertSame(target, target.load(source));
		RenderStateTest.assertSameProperties(source, target);
	}

	@Test
	public void keepsItsCopyApart() {
		final RenderState state = new RenderState();
		final RenderState copy = state.copy();
		state.lineWidth(5F).blend(BlendState.NORMAL);
		Assert.assertEquals(1F, copy.getLineWidth(), 0F);
		Assert.assertSame(BlendState.DISABLED, copy.getBlend());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingStencilState() {
		new RenderState().stencil(null);
	}

	private static RenderState create() {
		return new RenderState()
		.color(0.1F, 0.2F, 0.3F, 0.4F)
		.cull(true)
		.lineWidth(3F)
		.blend(BlendState.PREMULTIPLIED)
		.lighting(true)
		.depthTest(true)
		.alphaCutoff(0.5F)
		.colorWrite(false)
		.depthWrite(false)
		.lineSmooth(true)
		.stencil(StencilState.create(StencilFunction.EQUAL, 2, 0x0F, StencilOperation.ZERO, StencilOperation.INVERT, StencilOperation.REPLACE))
		.viewport(1, 2, 30, 40)
		.shader(new RecordingShader())
		.texture(new RecordingTexture())
		.textureWrap(TextureWrap.CLAMP_TO_BORDER)
		.frameBuffer(new RecordingFrameBuffer(8, 4))
		.textureFilter(TextureFilter.LINEAR);
	}

	private static void assertSameProperties(final RenderState expected, final RenderState actual) {
		Assert.assertEquals(expected.getRed(), actual.getRed(), 0F);
		Assert.assertEquals(expected.getGreen(), actual.getGreen(), 0F);
		Assert.assertEquals(expected.getBlue(), actual.getBlue(), 0F);
		Assert.assertEquals(expected.getAlpha(), actual.getAlpha(), 0F);
		Assert.assertEquals(expected.isCull(), actual.isCull());
		Assert.assertEquals(expected.getLineWidth(), actual.getLineWidth(), 0F);
		Assert.assertSame(expected.getBlend(), actual.getBlend());
		Assert.assertEquals(expected.isLighting(), actual.isLighting());
		Assert.assertEquals(expected.isDepthTest(), actual.isDepthTest());
		Assert.assertEquals(expected.getAlphaCutoff(), actual.getAlphaCutoff(), 0F);
		Assert.assertEquals(expected.isColorWrite(), actual.isColorWrite());
		Assert.assertEquals(expected.isDepthWrite(), actual.isDepthWrite());
		Assert.assertEquals(expected.isLineSmooth(), actual.isLineSmooth());
		Assert.assertSame(expected.getStencil(), actual.getStencil());
		Assert.assertEquals(expected.getViewportX(), actual.getViewportX());
		Assert.assertEquals(expected.getViewportY(), actual.getViewportY());
		Assert.assertEquals(expected.getViewportWidth(), actual.getViewportWidth());
		Assert.assertEquals(expected.getViewportHeight(), actual.getViewportHeight());
		Assert.assertSame(expected.getShader(), actual.getShader());
		Assert.assertSame(expected.getTexture(), actual.getTexture());
		Assert.assertSame(expected.getTextureWrap(), actual.getTextureWrap());
		Assert.assertSame(expected.getFrameBuffer(), actual.getFrameBuffer());
		Assert.assertSame(expected.getTextureFilter(), actual.getTextureFilter());
	}

}