package dev.joid.lib.render.modifier;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class VectorTest {

	@Test
	public void startsAtTheOrigin() {
		final Vector vector = Vector.create();
		Assert.assertEquals(0D, vector.getX(), 0D);
		Assert.assertEquals(0D, vector.getY(), 0D);
		Assert.assertEquals(0D, vector.getZ(), 0D);
	}

	@Test
	public void keepsAFlatVectorOnTheScreen() {
		final Vector vector = Vector.create(1.5D, -2.5D);
		Assert.assertEquals(1.5D, vector.getX(), 0D);
		Assert.assertEquals(-2.5D, vector.getY(), 0D);
		Assert.assertEquals(0D, vector.getZ(), 0D);
	}

	@Test
	public void holdsThreeCoordinates() {
		final Vector vector = Vector.create(1D, 2D, 3D);
		Assert.assertEquals(1D, vector.getX(), 0D);
		Assert.assertEquals(2D, vector.getY(), 0D);
		Assert.assertEquals(3D, vector.getZ(), 0D);
	}

	@Test
	public void readsItsSuppliersOnEveryCall() {
		final double[] position = {1D, 2D};
		final Vector vector = Vector.create(() -> position[0], () -> position[1]);
		Assert.assertEquals(1D, vector.getX(), 0D);
		Assert.assertEquals(0D, vector.getZ(), 0D);
		position[0] = 5D;
		position[1] = 6D;
		Assert.assertEquals(5D, vector.getX(), 0D);
		Assert.assertEquals(6D, vector.getY(), 0D);
	}

	@Test
	public void keepsTheSuppliersItIsGiven() {
		final Supplier<Double> x = () -> 1D;
		final Supplier<Double> y = () -> 2D;
		final Supplier<Double> z = () -> 3D;
		final Vector vector = Vector.create(x, y, z);
		Assert.assertSame(x, vector.getXSupplier());
		Assert.assertSame(y, vector.getYSupplier());
		Assert.assertSame(z, vector.getZSupplier());
		Assert.assertEquals(3D, vector.getZ(), 0D);
	}

	@Test
	public void pointsAlongASingleAxis() {
		Assert.assertEquals(4D, Vector.X(4D).getX(), 0D);
		Assert.assertEquals(0D, Vector.X(4D).getY(), 0D);
		Assert.assertEquals(5D, Vector.Y(5D).getY(), 0D);
		Assert.assertEquals(0D, Vector.Y(5D).getZ(), 0D);
		Assert.assertEquals(6D, Vector.Z(6D).getZ(), 0D);
		Assert.assertEquals(0D, Vector.Z(6D).getX(), 0D);
	}

	@Test
	public void followsASingleSuppliedAxis() {
		final double[] value = {1D};
		final Vector x = Vector.X(() -> value[0]);
		final Vector y = Vector.Y(() -> value[0]);
		final Vector z = Vector.Z(() -> value[0]);
		value[0] = 7D;
		Assert.assertEquals(7D, x.getX(), 0D);
		Assert.assertEquals(0D, x.getY() + x.getZ(), 0D);
		Assert.assertEquals(7D, y.getY(), 0D);
		Assert.assertEquals(0D, y.getX() + y.getZ(), 0D);
		Assert.assertEquals(7D, z.getZ(), 0D);
		Assert.assertEquals(0D, z.getX() + z.getY(), 0D);
	}

	@Test
	public void replacesEachCoordinate() {
		final Vector vector = Vector.create();
		Assert.assertSame(vector, vector.x(1D).y(2D).z(3D));
		Assert.assertEquals(1D, vector.getX(), 0D);
		Assert.assertEquals(2D, vector.getY(), 0D);
		Assert.assertEquals(3D, vector.getZ(), 0D);
	}

	@Test
	public void replacesEachCoordinateWithASupplier() {
		final double[] value = {1D};
		final Vector vector = Vector.create();
		Assert.assertSame(vector, vector.x(() -> value[0]).y(() -> value[0] * 2D).z(() -> value[0] * 3D));
		value[0] = 2D;
		Assert.assertEquals(2D, vector.getX(), 0D);
		Assert.assertEquals(4D, vector.getY(), 0D);
		Assert.assertEquals(6D, vector.getZ(), 0D);
	}

	@Test
	public void addsAnOffsetToItsCurrentPosition() {
		final double[] value = {1D};
		final Vector vector = Vector.create(() -> value[0], () -> 2D, () -> 3D);
		Assert.assertSame(vector, vector.add(10D, 20D, 30D));
		value[0] = 100D;
		Assert.assertEquals(11D, vector.getX(), 0D);
		Assert.assertEquals(22D, vector.getY(), 0D);
		Assert.assertEquals(33D, vector.getZ(), 0D);
	}

	@Test
	public void keepsFollowingItsSuppliersAfterASuppliedOffset() {
		final double[] value = {1D};
		final Vector vector = Vector.create(() -> value[0], () -> 2D, () -> 3D);
		Assert.assertSame(vector, vector.add(() -> value[0], () -> 1D, () -> -3D));
		value[0] = 5D;
		Assert.assertEquals(10D, vector.getX(), 0D);
		Assert.assertEquals(3D, vector.getY(), 0D);
		Assert.assertEquals(0D, vector.getZ(), 0D);
	}

}