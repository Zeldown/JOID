package dev.joid.lib.draw.shape;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;

public class DrawShapeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Before
	public void useAFractionalScale() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
	}

	@Test
	public void snapsTheEdgesOfABox() {
		DrawUtils.SHAPE.drawRect(10.3D, 20.6D, 100D, 50D, new Color(1F, 0F, 0F, 1F));
		final Draw draw = this.single(1F, 0F, 0F);
		Assert.assertEquals(Math.rint(10.3D * 1366D / 1920D), draw.getLeft(), 1E-3D);
		Assert.assertEquals(Math.rint(20.6D * 768D / 1080D), draw.getTop(), 1E-3D);
		Assert.assertEquals(Math.rint(110.3D * 1366D / 1920D), draw.getRight(), 1E-3D);
		Assert.assertEquals(Math.rint(70.6D * 768D / 1080D), draw.getBottom(), 1E-3D);
	}

	@Test
	public void keepsTouchingBoxesTogether() {
		DrawUtils.SHAPE.drawRect(0D, 10.3D, 100D, 40.4D, new Color(1F, 0F, 0F, 1F));
		DrawUtils.SHAPE.drawRect(0D, 50.7D, 100D, 30D, new Color(0F, 1F, 0F, 1F));
		Assert.assertEquals(this.single(1F, 0F, 0F).getBottom(), this.single(0F, 1F, 0F).getTop(), 1E-3D);
	}

	@Test
	public void drawsAThinRectangleWithTheSameThicknessEverywhere() {
		for (final double y : new double[] {10D, 10.2D, 10.5D, 10.8D, 133.3D}) {
			DrawUtils.SHAPE.drawRect(0D, y, 100D, 3D, new Color(0F, 0F, 1F, 1F));
		}

		final List<Draw> draws = this.bridges.getRender().getDraws(0F, 0F, 1F);
		Assert.assertEquals(5, draws.size());
		for (final Draw draw : draws) {
			Assert.assertEquals(2D, draw.getBottom() - draw.getTop(), 1E-3D);
			Assert.assertEquals(Math.rint(draw.getTop()), draw.getTop(), 1E-3D);
			Assert.assertEquals(1F, draw.getAlpha(), 0F);
		}
	}

	@Test
	public void lightensALineThinnerThanAPixel() {
		DrawUtils.SHAPE.drawRect(0D, 10.3D, 100D, 1D, new Color(1F, 1F, 1F, 1F));
		final Draw draw = this.single(1F, 1F, 1F);
		Assert.assertEquals(1D, draw.getBottom() - draw.getTop(), 1E-3D);
		Assert.assertEquals(768F / 1080F, draw.getAlpha(), 1E-4F);
	}

	@Test
	public void anchorsABorderOnItsBox() {
		DrawUtils.SHAPE.drawRect(10.3D, 10.3D, 100D, 50D, new Color(0F, 0F, 1F, 1F));
		DrawUtils.SHAPE.drawBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(1F, 0F, 0F, 1F), 3D);
		final Draw box = this.single(0F, 0F, 1F);
		final List<Draw> sides = this.bridges.getRender().getDraws(1F, 0F, 0F);
		Assert.assertEquals(4, sides.size());
		Assert.assertEquals(box.getTop(), sides.get(0).getBottom(), 1E-3D);
		Assert.assertEquals(box.getLeft(), sides.get(1).getRight(), 1E-3D);
		Assert.assertEquals(box.getBottom(), sides.get(2).getTop(), 1E-3D);
		Assert.assertEquals(box.getRight(), sides.get(3).getLeft(), 1E-3D);
		for (final Draw side : sides) {
			Assert.assertEquals(2D, Math.min(side.getBottom() - side.getTop(), side.getRight() - side.getLeft()), 1E-3D);
		}
	}

	@Test
	public void drawsACircleOnItsExactQuad() {
		DrawUtils.SHAPE.drawCircle(50.3D, 50.3D, new Color(0F, 1F, 0F, 1F), 10D);
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertEquals(40.3D * 1366D / 1920D, draw.getLeft(), 1E-4D);
		Assert.assertEquals(60.3D * 1366D / 1920D, draw.getRight(), 1E-4D);
	}

	@Test
	public void leavesARotatedRectangleExact() {
		this.bridges.getRender().pushMatrix();
		try {
			this.bridges.getRender().rotate(30D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawRect(10.3D, 20.6D, 100D, 50D, new Color(1F, 0F, 0F, 1F));
		} finally {
			this.bridges.getRender().popMatrix();
		}

		final Draw draw = this.single(1F, 0F, 0F);
		Assert.assertNotEquals(Math.rint(draw.getLeft()), draw.getLeft(), 1E-3D);
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

}