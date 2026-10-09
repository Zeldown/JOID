package dev.joid.lib.draw.raster;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.draw.DrawUtils;

public class ExternalRasterTest {

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@Test
	public void drawsIntoItsTargetAtThePixelSize() {
		final List<String> calls = new ArrayList<>();
		DrawUtils.RASTER.drawRaster(10D, 20D, 32D, 16D, (width, height) -> {
			calls.add(width + "x" + height);
			Assert.assertSame(DrawUtils.RASTER.getTarget(), this.render.getState().getFrameBuffer());
			Assert.assertEquals(32, this.render.getViewportWidth());
			Assert.assertEquals(16, this.render.getViewportHeight());
		});
		Assert.assertEquals(1, calls.size());
		Assert.assertEquals("32x16", calls.get(0));
	}

	@Test
	public void drawsItsTargetAsAPremultipliedQuad() {
		DrawUtils.RASTER.drawRaster(10D, 20D, 32D, 16D, (width, height) -> {});
		final IFrameBuffer target = DrawUtils.RASTER.getTarget();
		final Capture capture = this.render.getLast();
		Assert.assertSame(target.getTexture(), capture.getState().getTexture());
		Assert.assertSame(BlendState.PREMULTIPLIED, capture.getState().getBlend());
		Assert.assertSame(TextureFilter.NEAREST, capture.getState().getTextureFilter());
		Assert.assertEquals(10D, capture.getLeft(), 1E-3D);
		Assert.assertEquals(42D, capture.getRight(), 1E-3D);
		Assert.assertEquals(20D, capture.getTop(), 1E-3D);
		Assert.assertEquals(36D, capture.getBottom(), 1E-3D);
		Assert.assertEquals(0F, capture.getU(0), 0F);
		Assert.assertEquals(0F, capture.getV(0), 0F);
		Assert.assertEquals(32F / target.getWidth(), capture.getU(2), 1E-6F);
		Assert.assertEquals(16F / target.getHeight(), capture.getV(2), 1E-6F);
	}

	@Test
	public void restoresTheStateSetBeforeTheRaster() {
		DrawUtils.RASTER.drawRaster(10D, 20D, 32D, 16D, (width, height) -> {});
		Assert.assertNull(this.render.getState().getFrameBuffer());
		Assert.assertEquals(1920, this.render.getViewportWidth());
		Assert.assertEquals(1080, this.render.getViewportHeight());
		Assert.assertNull(this.render.getState().getTexture());
		Assert.assertSame(BlendState.DISABLED, this.render.getState().getBlend());
	}

	@Test
	public void followsTheRealPixelsOfAScaledView() {
		final List<String> calls = new ArrayList<>();
		this.render.resize(1366, 768);
		this.render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		DrawUtils.RASTER.drawRaster(0D, 0D, 100D, 50D, (width, height) -> calls.add(width + "x" + height));
		Assert.assertEquals("71x36", calls.get(0));
	}

	@Test
	public void keepsItsTargetForSmallerDrawsAndGrowsItForLargerOnes() {
		DrawUtils.RASTER.drawRaster(0D, 0D, 100D, 30D, (width, height) -> {});
		final IFrameBuffer first = DrawUtils.RASTER.getTarget();
		DrawUtils.RASTER.drawRaster(0D, 0D, 20D, 10D, (width, height) -> {});
		Assert.assertSame(first, DrawUtils.RASTER.getTarget());
		DrawUtils.RASTER.drawRaster(0D, 0D, 300D, 10D, (width, height) -> {});
		Assert.assertNotSame(first, DrawUtils.RASTER.getTarget());
		Assert.assertTrue(((RecordingFrameBuffer) first).isDeleted());
		Assert.assertTrue(DrawUtils.RASTER.getTarget().getWidth() >= 300);
	}

	@Test
	public void drawsNothingWithoutArea() {
		final List<String> calls = new ArrayList<>();
		DrawUtils.RASTER.drawRaster(0D, 0D, 0D, 10D, (width, height) -> calls.add("draw"));
		Assert.assertTrue(calls.isEmpty());
		Assert.assertTrue(this.render.getCaptures().isEmpty());
	}

}