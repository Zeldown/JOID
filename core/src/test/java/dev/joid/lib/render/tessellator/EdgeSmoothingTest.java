package dev.joid.lib.render.tessellator;

import javax.vecmath.Vector2d;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;

public class EdgeSmoothingTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void tellsAConvexPolygonFromAConcaveOne() {
		Assert.assertTrue(EdgeSmoothing.isConvex(new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(5D, 8D)));
		Assert.assertTrue(EdgeSmoothing.isConvex(new Vector2d(0D, 0D), new Vector2d(5D, 0D), new Vector2d(10D, 0D), new Vector2d(10D, 10D), new Vector2d(0D, 10D)));
		Assert.assertFalse(EdgeSmoothing.isConvex(new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(5D, 2D), new Vector2d(10D, 10D), new Vector2d(0D, 10D)));
		Assert.assertFalse(EdgeSmoothing.isConvex(new Vector2d(0D, 0D), new Vector2d(10D, 0D)));
		Assert.assertFalse(EdgeSmoothing.isConvex(new Vector2d(0D, 0D), new Vector2d(5D, 0D), new Vector2d(10D, 0D)));
	}

	@Test
	public void fadesTheEdgesOverOneUnitCenteredOnThem() {
		EdgeSmoothing.polygon(1F, 0F, 0F, 1F, new Vector2d(10D, 70D), new Vector2d(110D, 70D), new Vector2d(110D, 20D), new Vector2d(10D, 20D));
		final int[] buffer = Tessellator.inst().getRawBuffer();
		Assert.assertEquals(10.5F, Float.intBitsToFloat(buffer[0]), 0F);
		Assert.assertEquals(69.5F, Float.intBitsToFloat(buffer[1]), 0F);
		Assert.assertEquals(255, buffer[5] >>> 24);
		Assert.assertEquals(110.5F, Float.intBitsToFloat(buffer[8 * 8]), 0F);
		Assert.assertEquals(70.5F, Float.intBitsToFloat(buffer[8 * 8 + 1]), 0F);
		Assert.assertEquals(0, buffer[8 * 8 + 5] >>> 24);
		final Draw draw = this.bridges.getRender().getDraws().get(0);
		Assert.assertEquals(30, draw.getXs().length);
	}

	@Test
	public void lowersTheCoreOfAShapeThinnerThanOneUnit() {
		EdgeSmoothing.polygon(1F, 0F, 0F, 1F, new Vector2d(10D, 20.5D), new Vector2d(110D, 20.5D), new Vector2d(110D, 20D), new Vector2d(10D, 20D));
		final int[] buffer = Tessellator.inst().getRawBuffer();
		Assert.assertEquals(20.25F, Float.intBitsToFloat(buffer[1]), 1E-4F);
		Assert.assertEquals(127, buffer[5] >>> 24);
	}

	@Test
	public void mapsTheTextureOfARectOnItsSpreadEdges() {
		EdgeSmoothing.rect(10D, 20D, 110D, 70D, new double[] {0D, 0D, 1D, 1D}, 1F, 1F, 1F, 1F);
		final int[] buffer = Tessellator.inst().getRawBuffer();
		Assert.assertEquals(0.005F, Float.intBitsToFloat(buffer[3]), 1E-6F);
		Assert.assertEquals(0.99F, Float.intBitsToFloat(buffer[4]), 1E-6F);
		Assert.assertEquals(1.005F, Float.intBitsToFloat(buffer[8 * 8 + 3]), 1E-6F);
		Assert.assertEquals(1.01F, Float.intBitsToFloat(buffer[8 * 8 + 4]), 1E-6F);
	}

}