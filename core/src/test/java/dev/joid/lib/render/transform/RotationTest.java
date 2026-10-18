package dev.joid.lib.render.transform;

import org.junit.Assert;
import org.junit.Test;

public class RotationTest {

	@Test
	public void turnsTheYawAroundTheVerticalAxis() {
		Assert.assertEquals(0D, Rotation.YAW.getPitch(), 0D);
		Assert.assertEquals(1D, Rotation.YAW.getYaw(), 0D);
		Assert.assertEquals(0D, Rotation.YAW.getRoll(), 0D);
		Assert.assertEquals(1D, Rotation.of(1D, 0D, 0D).getYaw(), 0D);
	}

	@Test
	public void turnsThePitchAroundTheHorizontalAxis() {
		Assert.assertEquals(1D, Rotation.PITCH.getPitch(), 0D);
		Assert.assertEquals(0D, Rotation.PITCH.getYaw(), 0D);
		Assert.assertEquals(0D, Rotation.PITCH.getRoll(), 0D);
		Assert.assertEquals(1D, Rotation.of(0D, 1D, 0D).getPitch(), 0D);
		Assert.assertEquals(1D, Rotation.of(() -> 0D, () -> 1D, () -> 0D).getPitch(), 0D);
	}

	@Test
	public void turnsTheRollInTheScreenPlane() {
		Assert.assertEquals(0D, Rotation.ROLL.getPitch(), 0D);
		Assert.assertEquals(0D, Rotation.ROLL.getYaw(), 0D);
		Assert.assertEquals(1D, Rotation.ROLL.getRoll(), 0D);
		Assert.assertEquals(1D, Rotation.of(0D, 0D, 1D).getRoll(), 0D);
		Assert.assertEquals(1D, Rotation.of(() -> 0D, () -> 0D, () -> 1D).getRoll(), 0D);
	}

	@Test
	public void givesEachConstantItsOwnAxis() {
		final Rotation[] rotations = {Rotation.YAW, Rotation.PITCH, Rotation.ROLL};
		final double[] sum = new double[3];
		for (final Rotation rotation : rotations) {
			Assert.assertEquals(1D, rotation.getPitch() + rotation.getYaw() + rotation.getRoll(), 0D);
			sum[0] += rotation.getPitch();
			sum[1] += rotation.getYaw();
			sum[2] += rotation.getRoll();
		}

		Assert.assertArrayEquals(new double[] {1D, 1D, 1D}, sum, 0D);
	}

	@Test
	public void readsItsSuppliersOnEveryCall() {
		final double[] yaw = {0.5D};
		final Rotation rotation = Rotation.of(() -> yaw[0], () -> 0D, () -> 0D);
		Assert.assertEquals(0.5D, rotation.getYaw(), 0D);
		yaw[0] = 1D;
		Assert.assertEquals(1D, rotation.getYaw(), 0D);
		Assert.assertEquals(0D, rotation.getPitch(), 0D);
		Assert.assertEquals(0D, rotation.getRoll(), 0D);
		Assert.assertEquals(1D, rotation.getYawSupplier().get(), 0D);
	}

}