package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class ScaleTest {

	@Test
	public void keepsTheSizeByDefault() {
		final Scale scale = Scale.create();
		Assert.assertEquals(1D, scale.getWidth(), 0D);
		Assert.assertEquals(1D, scale.getHeight(), 0D);
		Assert.assertEquals(1D, scale.getDepth(), 0D);
	}

	@Test
	public void scalesEachAxis() {
		final Scale scale = Scale.create(2D, 0.5D, 3D);
		Assert.assertEquals(2D, scale.getWidth(), 0D);
		Assert.assertEquals(0.5D, scale.getHeight(), 0D);
		Assert.assertEquals(3D, scale.getDepth(), 0D);
	}

	@Test
	public void readsItsSuppliersOnEveryCall() {
		final double[] factor = {1D};
		final Supplier<Double> width = () -> factor[0];
		final Scale scale = Scale.create(width, () -> factor[0] * 2D, () -> factor[0] * 3D);
		factor[0] = 2D;
		Assert.assertSame(width, scale.getWidthSupplier());
		Assert.assertEquals(2D, scale.getWidth(), 0D);
		Assert.assertEquals(4D, scale.getHeight(), 0D);
		Assert.assertEquals(6D, scale.getDepth(), 0D);
	}

	@Test
	public void scalesASingleAxis() {
		final Scale width = Scale.create().width(2D);
		final Scale height = Scale.create().height(3D);
		final Scale depth = Scale.create().depth(4D);
		Assert.assertEquals(2D, width.getWidth(), 0D);
		Assert.assertEquals(2D, width.getHeight() + width.getDepth(), 0D);
		Assert.assertEquals(3D, height.getHeight(), 0D);
		Assert.assertEquals(2D, height.getWidth() + height.getDepth(), 0D);
		Assert.assertEquals(4D, depth.getDepth(), 0D);
		Assert.assertEquals(2D, depth.getWidth() + depth.getHeight(), 0D);
	}

	@Test
	public void followsASingleSuppliedAxis() {
		final double[] factor = {1D};
		final Scale width = Scale.create().width(() -> factor[0]);
		final Scale height = Scale.create().height(() -> factor[0]);
		final Scale depth = Scale.create().depth(() -> factor[0]);
		factor[0] = 5D;
		Assert.assertEquals(5D, width.getWidth(), 0D);
		Assert.assertEquals(2D, width.getHeight() + width.getDepth(), 0D);
		Assert.assertEquals(5D, height.getHeight(), 0D);
		Assert.assertEquals(2D, height.getWidth() + height.getDepth(), 0D);
		Assert.assertEquals(5D, depth.getDepth(), 0D);
		Assert.assertEquals(2D, depth.getWidth() + depth.getHeight(), 0D);
	}

	@Test
	public void replacesEachAxis() {
		final Scale scale = Scale.create();
		Assert.assertSame(scale, scale.width(2D).height(3D).depth(4D));
		Assert.assertEquals(2D, scale.getWidth(), 0D);
		Assert.assertEquals(3D, scale.getHeight(), 0D);
		Assert.assertEquals(4D, scale.getDepth(), 0D);
	}

	@Test
	public void replacesEachAxisWithASupplier() {
		final double[] factor = {1D};
		final Scale scale = Scale.create();
		Assert.assertSame(scale, scale.width(() -> factor[0]).height(() -> factor[0] + 1D).depth(() -> factor[0] + 2D));
		factor[0] = 3D;
		Assert.assertEquals(3D, scale.getWidth(), 0D);
		Assert.assertEquals(4D, scale.getHeight(), 0D);
		Assert.assertEquals(5D, scale.getDepth(), 0D);
	}

}