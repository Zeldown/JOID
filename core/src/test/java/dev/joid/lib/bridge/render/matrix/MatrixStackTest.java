package dev.joid.lib.bridge.render.matrix;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class MatrixStackTest {

	@Test
	public void startsFromTheIdentity() {
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F}, new MatrixStack().getMatrix(), 0F);
	}

	@Test
	public void restoresThePushedMatrixOnPop() {
		final MatrixStack stack = new MatrixStack();
		stack.translate(1D, 2D, 3D);
		stack.push();
		stack.scale(2D, 2D, 2D);
		stack.translate(5D, 0D, 0D);
		stack.pop();
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 1F, 2F, 3F, 1F}, stack.getMatrix(), 0F);
	}

	@Test(expected = NoSuchElementException.class)
	public void refusesToPopAnEmptyStack() {
		new MatrixStack().pop();
	}

	@Test
	public void goesBackToTheIdentity() {
		final MatrixStack stack = new MatrixStack();
		stack.translate(1D, 2D, 3D);
		stack.rotate(30D, 0D, 0D, 1D);
		stack.identity();
		Assert.assertArrayEquals(new MatrixStack().getMatrix(), stack.getMatrix(), 0F);
	}

	@Test
	public void loadsACopyOfTheGivenMatrix() {
		final float[] matrix = {2F, 0F, 0F, 0F, 0F, 3F, 0F, 0F, 0F, 0F, 1F, 0F, 4F, 5F, 0F, 1F};
		final MatrixStack stack = new MatrixStack();
		stack.load(matrix);
		matrix[12] = 9F;
		stack.translate(1D, 1D, 0D);
		Assert.assertArrayEquals(new float[] {2F, 0F, 0F, 0F, 0F, 3F, 0F, 0F, 0F, 0F, 1F, 0F, 6F, 8F, 0F, 1F}, stack.getMatrix(), 0F);
	}

	@Test
	public void scalesEveryAxis() {
		final MatrixStack stack = new MatrixStack();
		stack.scale(2D, 3D, 4D);
		Assert.assertArrayEquals(new float[] {2F, 0F, 0F, 0F, 0F, 3F, 0F, 0F, 0F, 0F, 4F, 0F, 0F, 0F, 0F, 1F}, stack.getMatrix(), 0F);
	}

	@Test
	public void translatesInItsOwnSpace() {
		final MatrixStack stack = new MatrixStack();
		stack.scale(2D, 3D, 4D);
		stack.translate(1D, 1D, 1D);
		Assert.assertEquals(2F, stack.getMatrix()[12], 0F);
		Assert.assertEquals(3F, stack.getMatrix()[13], 0F);
		Assert.assertEquals(4F, stack.getMatrix()[14], 0F);
		Assert.assertEquals(1F, stack.getMatrix()[15], 0F);
	}

	@Test
	public void turnsTheXAxisOntoTheYAxisAQuarterTurnAroundZ() {
		final MatrixStack stack = new MatrixStack();
		stack.rotate(90D, 0D, 0D, 5D);
		final float[] matrix = stack.getMatrix();
		Assert.assertEquals(0F, matrix[0], 1E-6F);
		Assert.assertEquals(1F, matrix[1], 1E-6F);
		Assert.assertEquals(-1F, matrix[4], 1E-6F);
		Assert.assertEquals(0F, matrix[5], 1E-6F);
		Assert.assertEquals(1F, matrix[10], 1E-6F);
	}

	@Test
	public void rotatesAfterItsTranslation() {
		final MatrixStack stack = new MatrixStack();
		stack.translate(10D, 0D, 0D);
		stack.rotate(90D, 0D, 0D, 1D);
		stack.translate(5D, 0D, 0D);
		Assert.assertEquals(10F, stack.getMatrix()[12], 1E-5F);
		Assert.assertEquals(5F, stack.getMatrix()[13], 1E-5F);
	}

	@Test
	public void ignoresARotationWithoutAxis() {
		final MatrixStack stack = new MatrixStack();
		stack.rotate(45D, 0D, 0D, 0D);
		Assert.assertArrayEquals(new MatrixStack().getMatrix(), stack.getMatrix(), 0F);
	}

	@Test
	public void mapsTheCanvasOntoTheClipSpace() {
		final MatrixStack stack = new MatrixStack();
		stack.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final float[] matrix = stack.getMatrix();
		Assert.assertEquals(-1F, matrix[12], 1E-6F);
		Assert.assertEquals(1F, matrix[13], 1E-6F);
		Assert.assertEquals(1F, matrix[0] * 1920F + matrix[12], 1E-6F);
		Assert.assertEquals(-1F, matrix[5] * 1080F + matrix[13], 1E-6F);
		Assert.assertEquals(-1F, matrix[14], 1E-6F);
		Assert.assertEquals(1F, matrix[10] * -10000F + matrix[14], 1E-6F);
	}

	@Test
	public void multipliesOnTheRight() {
		final MatrixStack stack = new MatrixStack();
		stack.translate(10D, 0D, 0D);
		stack.multiply(new float[] {2F, 0F, 0F, 0F, 0F, 2F, 0F, 0F, 0F, 0F, 2F, 0F, 1F, 0F, 0F, 1F});
		Assert.assertArrayEquals(new float[] {2F, 0F, 0F, 0F, 0F, 2F, 0F, 0F, 0F, 0F, 2F, 0F, 11F, 0F, 0F, 1F}, stack.getMatrix(), 0F);
	}

	@Test
	public void invertsAndTransposesItsScaleForTheNormals() {
		final MatrixStack stack = new MatrixStack();
		stack.translate(40D, 50D, 60D);
		stack.scale(2D, 4D, 1D);
		Assert.assertArrayEquals(new float[] {0.5F, 0F, 0F, 0F, 0.25F, 0F, 0F, 0F, 1F}, stack.getNormalMatrix(), 1E-6F);
	}

	@Test
	public void keepsItsRotationForTheNormals() {
		final MatrixStack stack = new MatrixStack();
		stack.rotate(90D, 0D, 0D, 1D);
		final float[] matrix = stack.getMatrix();
		Assert.assertArrayEquals(new float[] {matrix[0], matrix[1], matrix[2], matrix[4], matrix[5], matrix[6], matrix[8], matrix[9], matrix[10]}, stack.getNormalMatrix(), 1E-6F);
	}

	@Test
	public void flattensTheNormalsOfASingularMatrix() {
		final MatrixStack stack = new MatrixStack();
		stack.scale(0D, 1D, 1D);
		Assert.assertArrayEquals(new float[9], stack.getNormalMatrix(), 0F);
	}

}