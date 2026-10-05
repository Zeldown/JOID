package dev.joid.lib.resource.dto.animation;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas.Blend;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas.Disposal;

public class ResourceAnimationCanvasTest {

	private static final int RED   = 0xFFFF0000;
	private static final int BLUE  = 0xFF0000FF;
	private static final int GHOST = 0x800000FF;

	@Test
	public void paintsAFrameInsideTheCanvas() {
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(2, 2);
		final int[] frame = canvas.compose(new int[] {ResourceAnimationCanvasTest.RED}, 1, 1, 1, 1, Blend.SOURCE, Disposal.NONE);
		Assert.assertEquals(0, frame[0] >>> 24);
		Assert.assertEquals(ResourceAnimationCanvasTest.RED, frame[3]);
	}

	@Test
	public void blendsTranslucentPixelsOverTheCanvas() {
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(1, 1);
		canvas.compose(new int[] {ResourceAnimationCanvasTest.RED}, 0, 0, 1, 1, Blend.SOURCE, Disposal.NONE);
		final int over = canvas.compose(new int[] {ResourceAnimationCanvasTest.GHOST}, 0, 0, 1, 1, Blend.OVER, Disposal.NONE)[0];
		Assert.assertEquals(255, over >>> 24);
		Assert.assertEquals(127, over >> 16 & 0xFF, 1);
		Assert.assertEquals(128, over & 0xFF, 1);
		Assert.assertEquals(ResourceAnimationCanvasTest.GHOST, canvas.compose(new int[] {ResourceAnimationCanvasTest.GHOST}, 0, 0, 1, 1, Blend.SOURCE, Disposal.NONE)[0]);
	}

	@Test
	public void keepsTheCanvasWhenTheFrameIsTransparent() {
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(1, 1);
		canvas.compose(new int[] {ResourceAnimationCanvasTest.RED}, 0, 0, 1, 1, Blend.SOURCE, Disposal.NONE);
		Assert.assertEquals(ResourceAnimationCanvasTest.RED, canvas.compose(new int[] {0}, 0, 0, 1, 1, Blend.OVER, Disposal.NONE)[0]);
	}

	@Test
	public void clearsTheFrameAreaAfterABackgroundDisposal() {
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(2, 1);
		canvas.compose(new int[] {ResourceAnimationCanvasTest.RED, ResourceAnimationCanvasTest.RED}, 0, 0, 2, 1, Blend.SOURCE, Disposal.NONE);
		Assert.assertEquals(ResourceAnimationCanvasTest.BLUE, canvas.compose(new int[] {ResourceAnimationCanvasTest.BLUE}, 1, 0, 1, 1, Blend.SOURCE, Disposal.BACKGROUND)[1]);
		final int[] next = canvas.compose(new int[] {0}, 1, 0, 1, 1, Blend.OVER, Disposal.NONE);
		Assert.assertEquals(ResourceAnimationCanvasTest.RED, next[0]);
		Assert.assertEquals(0, next[1] >>> 24);
	}

	@Test
	public void restoresThePreviousCanvasAfterAPreviousDisposal() {
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(1, 1);
		canvas.compose(new int[] {ResourceAnimationCanvasTest.RED}, 0, 0, 1, 1, Blend.SOURCE, Disposal.NONE);
		Assert.assertEquals(ResourceAnimationCanvasTest.BLUE, canvas.compose(new int[] {ResourceAnimationCanvasTest.BLUE}, 0, 0, 1, 1, Blend.SOURCE, Disposal.PREVIOUS)[0]);
		Assert.assertEquals(ResourceAnimationCanvasTest.RED, canvas.compose(new int[] {0}, 0, 0, 1, 1, Blend.OVER, Disposal.NONE)[0]);
	}

	@Test
	public void clipsAFrameOutsideTheCanvas() {
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(1, 1);
		Assert.assertEquals(ResourceAnimationCanvasTest.BLUE, canvas.compose(new int[] {ResourceAnimationCanvasTest.RED, ResourceAnimationCanvasTest.BLUE}, -1, 0, 2, 1, Blend.SOURCE, Disposal.NONE)[0]);
	}

}