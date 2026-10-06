package dev.joid.lib.render.modifier;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class ScaleTest {

	@Test
	public void keepsTheSizeByDefault() {
		final Scale scale = Scale.create();
		Assert.assertEquals(1D, scale.getRawX(), 0D);
		Assert.assertEquals(1D, scale.getRawY(), 0D);
		Assert.assertEquals(1D, scale.getRawZ(), 0D);
	}

	@Test
	public void scalesEachAxis() {
		final Scale scale = Scale.create(2D, 0.5D, 3D);
		Assert.assertEquals(2D, scale.getRawX(), 0D);
		Assert.assertEquals(0.5D, scale.getRawY(), 0D);
		Assert.assertEquals(3D, scale.getRawZ(), 0D);
	}

	@Test
	public void readsItsSuppliersOnEveryCall() {
		final double[] factor = {1D};
		final Supplier<Double> width = () -> factor[0];
		final Scale scale = Scale.create(width, () -> factor[0] * 2D, () -> factor[0] * 3D);
		factor[0] = 2D;
		Assert.assertSame(width, scale.getRawXSupplier());
		Assert.assertEquals(2D, scale.getRawX(), 0D);
		Assert.assertEquals(4D, scale.getRawY(), 0D);
		Assert.assertEquals(6D, scale.getRawZ(), 0D);
	}

	@Test
	public void scalesASingleAxis() {
		final Scale width = Scale.WIDTH(2D);
		final Scale height = Scale.HEIGHT(3D);
		final Scale depth = Scale.DEPTH(4D);
		Assert.assertEquals(2D, width.getRawX(), 0D);
		Assert.assertEquals(2D, width.getRawY() + width.getRawZ(), 0D);
		Assert.assertEquals(3D, height.getRawY(), 0D);
		Assert.assertEquals(2D, height.getRawX() + height.getRawZ(), 0D);
		Assert.assertEquals(4D, depth.getRawZ(), 0D);
		Assert.assertEquals(2D, depth.getRawX() + depth.getRawY(), 0D);
	}

	@Test
	public void followsASingleSuppliedAxis() {
		final double[] factor = {1D};
		final Scale width = Scale.WIDTH(() -> factor[0]);
		final Scale height = Scale.HEIGHT(() -> factor[0]);
		final Scale depth = Scale.DEPTH(() -> factor[0]);
		factor[0] = 5D;
		Assert.assertEquals(5D, width.getRawX(), 0D);
		Assert.assertEquals(2D, width.getRawY() + width.getRawZ(), 0D);
		Assert.assertEquals(5D, height.getRawY(), 0D);
		Assert.assertEquals(2D, height.getRawX() + height.getRawZ(), 0D);
		Assert.assertEquals(5D, depth.getRawZ(), 0D);
		Assert.assertEquals(2D, depth.getRawX() + depth.getRawY(), 0D);
	}

	@Test
	public void replacesEachAxis() {
		final Scale scale = Scale.create();
		Assert.assertSame(scale, scale.width(2D).height(3D).depth(4D));
		Assert.assertEquals(2D, scale.getRawX(), 0D);
		Assert.assertEquals(3D, scale.getRawY(), 0D);
		Assert.assertEquals(4D, scale.getRawZ(), 0D);
	}

	@Test
	public void replacesEachAxisWithASupplier() {
		final double[] factor = {1D};
		final Scale scale = Scale.create();
		Assert.assertSame(scale, scale.width(() -> factor[0]).height(() -> factor[0] + 1D).depth(() -> factor[0] + 2D));
		factor[0] = 3D;
		Assert.assertEquals(3D, scale.getRawX(), 0D);
		Assert.assertEquals(4D, scale.getRawY(), 0D);
		Assert.assertEquals(5D, scale.getRawZ(), 0D);
	}

}