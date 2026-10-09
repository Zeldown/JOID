package dev.joid.lib.bridge.render;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.SamplerBinding;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import dev.joid.lib.bridge.render.texture.TextureWrap;

public class RenderBridgeTest {

	@Test
	public void startsWithAnOpaqueWhiteAndThinLines() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		Assert.assertEquals(1F, render.getState().getRed(), 0F);
		Assert.assertEquals(1F, render.getState().getAlpha(), 0F);
		Assert.assertEquals(1F, render.getLineWidth(), 0F);
		Assert.assertFalse(render.isLineSmooth());
		Assert.assertNull(render.getShader());
		Assert.assertSame(BlendState.DISABLED, render.getState().getBlend());
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F}, render.getModelView().getMatrix(), 0F);
	}

	@Test
	public void runsTheFrameCommandsOfTheBackendOnce() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.beginFrame();
		Assert.assertTrue(render.isFrameActive());
		Assert.assertEquals(1, render.getFrameCommands());
		render.endFrame();
		Assert.assertFalse(render.isFrameActive());
		Assert.assertEquals(0, render.getFrameCommands());
	}

	@Test
	public void refusesAFrameThatAlreadyBegan() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.beginFrame();
		try {
			render.beginFrame();
			Assert.fail("A second beginFrame() must be refused");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The JOID frame has already begun, call endFrame() first", expected.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToEndAFrameThatNeverBegan() {
		new RecordingRenderBridge().endFrame();
	}

	@Test
	public void clearsNoColorWhileTheColorIsMasked() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.colorMask(false);
		render.clearColor(1F, 0F, 0F, 1F);
		Assert.assertEquals(0, render.getColorClears());
		render.colorMask(true);
		render.clearColor(1F, 0F, 0F, 1F);
		Assert.assertEquals(1, render.getColorClears());
	}

	@Test
	public void clearsTheStencilOfTheScreenOnly() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.frameBuffer(render.createFrameBuffer(4, 4));
		render.clearStencil();
		Assert.assertEquals(0, render.getStencilClears());
		render.frameBuffer(null);
		render.clearStencil();
		Assert.assertEquals(1, render.getStencilClears());
	}

	@Test
	public void restoresThePushedStateOnPop() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.color(0.2F, 0.4F, 0.6F, 0.8F);
		render.pushState();
		render.color(0.5F, 0.5F, 0.5F, 0.5F);
		render.lineWidth(3F);
		render.popState();
		Assert.assertEquals(0.2F, render.getState().getRed(), 0F);
		Assert.assertEquals(0.4F, render.getState().getGreen(), 0F);
		Assert.assertEquals(0.6F, render.getState().getBlue(), 0F);
		Assert.assertEquals(0.8F, render.getState().getAlpha(), 0F);
		Assert.assertEquals(1F, render.getLineWidth(), 0F);
		Assert.assertTrue(render.getStateStack().isEmpty());
	}

	@Test
	public void drawsToTheWholeWindowOnScreen() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final RecordingRenderBridge expected = new RecordingRenderBridge();
		render.frameBuffer(render.createFrameBuffer(4, 4));
		render.viewport(1, 2, 3, 4);
		render.screen(1280, 720);
		expected.ortho(0D, 1280D, 720D, 0D, 0D, 10000D);
		Assert.assertNull(render.getState().getFrameBuffer());
		Assert.assertEquals(0, render.getState().getViewportX());
		Assert.assertEquals(0, render.getState().getViewportY());
		Assert.assertEquals(1280, render.getViewportWidth());
		Assert.assertEquals(720, render.getViewportHeight());
		Assert.assertArrayEquals(expected.getProjection().getMatrix(), render.getProjection().getMatrix(), 0F);
	}

	@Test(expected = NoSuchElementException.class)
	public void refusesToPopAnEmptyStateStack() {
		new RecordingRenderBridge().popState();
	}

	@Test
	public void storesItsDepthCullingLightingAndColorMask() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.depth(true, false);
		render.cull(true);
		render.lighting(true);
		render.colorMask(false);
		final RenderState state = render.getState();
		Assert.assertTrue(state.isDepthTest());
		Assert.assertFalse(state.isDepthWrite());
		Assert.assertTrue(state.isCull());
		Assert.assertTrue(state.isLighting());
		Assert.assertFalse(state.isColorMask());
	}

	@Test
	public void enablesTheAlphaTestWithItsThreshold() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		Assert.assertFalse(render.getState().isAlphaTest());
		render.alphaTest(0.5F);
		Assert.assertTrue(render.getState().isAlphaTest());
		Assert.assertEquals(0.5F, render.getState().getAlphaThreshold(), 0F);
	}

	@Test
	public void disablesTheAlphaTestAtZero() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.alphaTest(0.5F);
		render.alphaTest(0F);
		Assert.assertFalse(render.getState().isAlphaTest());
		Assert.assertEquals(0F, render.getState().getAlphaThreshold(), 0F);
	}

	@Test
	public void storesItsLines() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.lineWidth(2.5F);
		render.lineSmooth(true);
		Assert.assertEquals(2.5F, render.getLineWidth(), 0F);
		Assert.assertTrue(render.isLineSmooth());
	}

	@Test
	public void storesItsBlending() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.blend(BlendState.NORMAL);
		Assert.assertSame(BlendState.NORMAL, render.getState().getBlend());
	}

	@Test
	public void storesItsStencil() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.stencilTest(true);
		render.stencilFunction(StencilFunction.EQUAL, 2, 0x0F);
		render.stencilOperation(StencilOperation.ZERO, StencilOperation.REPLACE, StencilOperation.INCREMENT);
		final RenderState state = render.getState();
		Assert.assertTrue(state.isStencilTest());
		Assert.assertSame(StencilFunction.EQUAL, state.getStencilFunction());
		Assert.assertEquals(2, state.getStencilReference());
		Assert.assertEquals(0x0F, state.getStencilMask());
		Assert.assertSame(StencilOperation.ZERO, state.getStencilFail());
		Assert.assertSame(StencilOperation.REPLACE, state.getStencilDepthFail());
		Assert.assertSame(StencilOperation.INCREMENT, state.getStencilPass());
	}

	@Test
	public void storesItsViewport() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.viewport(10, 20, 300, 400);
		Assert.assertEquals(10, render.getState().getViewportX());
		Assert.assertEquals(20, render.getState().getViewportY());
		Assert.assertEquals(300, render.getViewportWidth());
		Assert.assertEquals(400, render.getViewportHeight());
	}

	@Test
	public void bindsATextureUntilItIsReset() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final RecordingTexture texture = new RecordingTexture();
		render.texture(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_BORDER);
		Assert.assertSame(texture, render.getState().getTexture());
		Assert.assertSame(TextureFilter.LINEAR, render.getState().getTextureFilter());
		Assert.assertSame(TextureWrap.CLAMP_TO_BORDER, render.getState().getTextureWrap());
		render.resetTexture();
		Assert.assertNull(render.getState().getTexture());
		Assert.assertSame(TextureFilter.NEAREST, render.getState().getTextureFilter());
		Assert.assertSame(TextureWrap.REPEAT, render.getState().getTextureWrap());
	}

	@Test
	public void bindsAFrameBufferAndAShader() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final RecordingFrameBuffer frameBuffer = new RecordingFrameBuffer(64, 32);
		final RecordingShader shader = new RecordingShader();
		render.frameBuffer(frameBuffer);
		render.shader(shader);
		Assert.assertSame(frameBuffer, render.getState().getFrameBuffer());
		Assert.assertSame(shader, render.getShader());
		render.frameBuffer(null);
		render.shader(null);
		Assert.assertNull(render.getState().getFrameBuffer());
		Assert.assertNull(render.getShader());
	}

	@Test
	public void transformsItsModelView() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.translate(10D, 20D, 30D);
		render.pushMatrix();
		render.scale(2D, 2D, 2D);
		render.rotate(90D, 0D, 0D, 1D);
		Assert.assertEquals(2F, render.getModelView().getMatrix()[1], 1E-6F);
		render.popMatrix();
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 10F, 20F, 30F, 1F}, render.getModelView().getMatrix(), 0F);
		render.loadIdentity();
		Assert.assertEquals(0F, render.getModelView().getMatrix()[12], 0F);
	}

	@Test
	public void restoresThePushedProjection() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final float[] canvas = render.getProjection().getMatrix().clone();
		render.pushProjection();
		render.ortho(0D, 100D, 100D, 0D, -1D, 1D);
		Assert.assertEquals(0.02F, render.getProjection().getMatrix()[0], 1E-6F);
		render.popProjection();
		Assert.assertArrayEquals(canvas, render.getProjection().getMatrix(), 0F);
	}

	@Test
	public void readsThePixelGridOfItsMatrices() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		render.viewport(0, 0, 3840, 2160);
		render.translate(100D, 0D, 0D);
		Assert.assertEquals(2D, render.getPixelGrid().getScaleX(), 1E-4D);
		Assert.assertEquals(200D, render.getPixelGrid().toScreenX(0D), 1E-3D);
	}

	@Test
	public void leavesTheMatrixStillWithoutMotion() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		render.viewport(0, 0, 1366, 768);
		render.translate(10.3D, 0D, 0D);
		final float[] before = render.getModelView().getMatrix().clone();
		render.quantize(0D, 0D);
		Assert.assertArrayEquals(before, render.getModelView().getMatrix(), 0F);
	}

	@Test
	public void roundsAMotionToWholePixels() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		render.viewport(0, 0, 1366, 768);
		render.translate(10.3D, 0D, 0D);
		final PixelGrid rest = render.getPixelGrid();
		render.translate(0.6D, 2.2D, 0D);
		render.quantize(0.6D, 2.2D);
		final double motionX = render.getPixelGrid().toScreenX(0D) - rest.toScreenX(0D);
		final double motionY = render.getPixelGrid().toScreenY(0D) - rest.toScreenY(0D);
		Assert.assertEquals(Math.rint(motionX), motionX, 1E-3D);
		Assert.assertEquals(Math.rint(motionY), motionY, 1E-3D);
		Assert.assertEquals(0.6D * 1366D / 1920D, motionX, 0.5D + 1E-3D);
	}

	@Test
	public void forgetsItsRoundingOnceTheMatrixIsPopped() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		render.viewport(0, 0, 1366, 768);
		final float[] before = render.getModelView().getMatrix().clone();
		render.pushMatrix();
		render.quantize(0.6D, 0D);
		Assert.assertNotEquals(before[12], render.getModelView().getMatrix()[12], 0F);
		render.popMatrix();
		Assert.assertArrayEquals(before, render.getModelView().getMatrix(), 0F);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingBlendState() {
		new RecordingRenderBridge().blend(null);
	}

	@Test
	public void resolvesTheTextureOfASampler() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final ITexture texture = new RecordingTexture().allocate(4, 4).mipmap(true);
		final UniformSampler sampler = UniformSampler.create("mask", 1).value(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);
		render.texture(new RecordingTexture().allocate(2, 2), TextureFilter.NEAREST, TextureWrap.REPEAT);
		final SamplerBinding binding = render.resolveSampler(sampler);
		Assert.assertSame(texture, binding.getTexture());
		Assert.assertSame(TextureSampling.of(TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE, true), binding.getSampling());
	}

	@Test
	public void resolvesAnUnsetSamplerToTheBoundTexture() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final ITexture texture = new RecordingTexture().allocate(2, 2);
		render.texture(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_BORDER);
		final SamplerBinding binding = render.resolveSampler(UniformSampler.create("mask", 1));
		Assert.assertSame(texture, binding.getTexture());
		Assert.assertSame(TextureSampling.of(TextureFilter.LINEAR, TextureWrap.CLAMP_TO_BORDER, false), binding.getSampling());
	}

	@Test
	public void resolvesTheEmptyTextureWithoutAnAllocatedTexture() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.texture(new RecordingTexture(), TextureFilter.LINEAR, TextureWrap.CLAMP_TO_BORDER);
		final SamplerBinding binding = render.resolveSampler(UniformSampler.create("mask", 1).value(new RecordingTexture(), TextureFilter.LINEAR, TextureWrap.REPEAT));
		Assert.assertSame(render.getEmptyTexture(), binding.getTexture());
		Assert.assertSame(TextureSampling.of(TextureFilter.NEAREST, TextureWrap.REPEAT, false), binding.getSampling());
		Assert.assertSame(render.getEmptyTexture(), render.resolveTexture().getTexture());
	}

}