package dev.joid.lib.bridge.render.matrix;

import org.junit.Assert;
import org.junit.Test;

public class DepthRangeTest {

	@Test
	public void mapsTheNearPlaneToZeroAndTheFarPlaneToOne() {
		final MatrixStack stack = new MatrixStack();
		stack.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final float[] matrix = DepthRange.toZeroToOne(stack.getMatrix());
		Assert.assertEquals(0F, DepthRangeTest.depth(matrix, 0F), 1E-6F);
		Assert.assertEquals(1F, DepthRangeTest.depth(matrix, -10000F), 1E-6F);
		Assert.assertEquals(-1F, DepthRangeTest.depth(stack.getMatrix(), 0F), 1E-6F);
	}

	@Test
	public void keepsTheOtherRows() {
		final MatrixStack stack = new MatrixStack();
		stack.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final float[] matrix = DepthRange.toZeroToOne(stack.getMatrix());
		for (int column = 0; column < 4; column++) {
			for (final int row : new int[] {0, 1, 3}) {
				Assert.assertEquals(stack.getMatrix()[column * 4 + row], matrix[column * 4 + row], 0F);
			}
		}
	}

	@Test
	public void leavesTheProjectionUntouched() {
		final MatrixStack stack = new MatrixStack();
		stack.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final float[] projection = stack.getMatrix().clone();
		DepthRange.toZeroToOne(stack.getMatrix());
		Assert.assertArrayEquals(projection, stack.getMatrix(), 0F);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAMatrixOfTheWrongSize() {
		DepthRange.toZeroToOne(new float[9]);
	}

	private static float depth(final float[] matrix, final float z) {
		final float clipZ = matrix[10] * z + matrix[14];
		final float clipW = matrix[11] * z + matrix[15];
		return clipZ / clipW;
	}

}