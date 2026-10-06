package dev.joid.lib.render.modifier;

import org.junit.Assert;
import org.junit.Test;

public class RotationTest {

	@Test
	public void startsWithoutAxis() {
		final Rotation rotation = Rotation.create();
		Assert.assertEquals(0D, rotation.getRawX(), 0D);
		Assert.assertEquals(0D, rotation.getRawY(), 0D);
		Assert.assertEquals(0D, rotation.getRawZ(), 0D);
	}

	@Test
	public void turnsTheYawAroundTheVerticalAxis() {
		Assert.assertEquals(0D, Rotation.YAW.getRawX(), 0D);
		Assert.assertEquals(1D, Rotation.YAW.getRawY(), 0D);
		Assert.assertEquals(0D, Rotation.YAW.getRawZ(), 0D);
		Assert.assertEquals(1D, Rotation.create(1D, 0D, 0D).getRawY(), 0D);
	}

	@Test
	public void givesEachConstantItsOwnAxis() {
		final Rotation[] rotations = {Rotation.YAW, Rotation.PITCH, Rotation.ROLL};
		final double[] sum = new double[3];
		for (final Rotation rotation : rotations) {
			Assert.assertEquals(1D, rotation.getRawX() + rotation.getRawY() + rotation.getRawZ(), 0D);
			sum[0] += rotation.getRawX();
			sum[1] += rotation.getRawY();
			sum[2] += rotation.getRawZ();
		}

		Assert.assertArrayEquals(new double[] {1D, 1D, 1D}, sum, 0D);
	}

	@Test
	public void readsItsSuppliersOnEveryCall() {
		final double[] yaw = {0.5D};
		final Rotation rotation = Rotation.create(() -> yaw[0], () -> 0D, () -> 0D);
		Assert.assertEquals(0.5D, rotation.getRawY(), 0D);
		yaw[0] = 1D;
		Assert.assertEquals(1D, rotation.getRawY(), 0D);
		Assert.assertEquals(0D, rotation.getRawX(), 0D);
		Assert.assertEquals(0D, rotation.getRawZ(), 0D);
		Assert.assertEquals(1D, rotation.getRawYSupplier().get(), 0D);
	}

}