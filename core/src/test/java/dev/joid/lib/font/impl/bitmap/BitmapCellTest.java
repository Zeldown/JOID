package dev.joid.lib.font.impl.bitmap;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;

public class BitmapCellTest {

	private static final ITexture TEXTURE = new RecordingTexture().allocate(128, 48);

	@Test
	public void keepsItsTexels() {
		final BitmapCell cell = BitmapCell.create(BitmapCellTest.TEXTURE, 8, 16, 24, 48);
		Assert.assertSame(BitmapCellTest.TEXTURE, cell.getTexture());
		Assert.assertArrayEquals(new int[] {8, 16, 24, 48}, new int[] {cell.getTexelLeft(), cell.getTexelTop(), cell.getTexelRight(), cell.getTexelBottom()});
		Assert.assertEquals(16, cell.getTexelWidth());
		Assert.assertEquals(32, cell.getTexelHeight());
	}

	@Test
	public void boundsItsTexelsAsFontPixelsByDefault() {
		final BitmapCell cell = BitmapCell.create(BitmapCellTest.TEXTURE, 8, 16, 24, 48);
		Assert.assertArrayEquals(new double[] {0D, 0D, 16D, 32D}, new double[] {cell.getLeft(), cell.getTop(), cell.getRight(), cell.getBottom()}, 0D);
		Assert.assertFalse(cell.isBold());
		Assert.assertFalse(cell.isGrayscale());
	}

	@Test
	public void takesItsBoundsAndStyle() {
		final BitmapCell cell = BitmapCell.create(BitmapCellTest.TEXTURE, 0, 0, 16, 16).bounds(0.5D, -1D, 8.5D, 7D).bold(true).grayscale(true);
		Assert.assertArrayEquals(new double[] {0.5D, -1D, 8.5D, 7D}, new double[] {cell.getLeft(), cell.getTop(), cell.getRight(), cell.getBottom()}, 0D);
		Assert.assertEquals(8D, cell.getWidth(), 0D);
		Assert.assertEquals(8D, cell.getHeight(), 0D);
		Assert.assertTrue(cell.isBold());
		Assert.assertTrue(cell.isGrayscale());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyCell() {
		BitmapCell.create(BitmapCellTest.TEXTURE, 8, 0, 8, 8);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesEmptyBounds() {
		BitmapCell.create(BitmapCellTest.TEXTURE, 0, 0, 8, 8).bounds(0D, 4D, 8D, 4D);
	}

}