package dev.joid.lib.bridge.render.texture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MipmapChainTest {

	@Test
	public void keepsOneLevelWithoutMipmaps() {
		Assert.assertEquals(1, MipmapChain.of(256, 256, false).getLevels());
	}

	@Test
	public void keepsOneLevelForASinglePixel() {
		Assert.assertEquals(1, MipmapChain.of(1, 1, true).getLevels());
	}

	@Test
	public void countsTheLevelsFromTheLargestSide() {
		Assert.assertEquals(9, MipmapChain.of(256, 16, true).getLevels());
		Assert.assertEquals(9, MipmapChain.of(16, 256, true).getLevels());
	}

	@Test
	public void halvesEachLevelDownToOnePixel() {
		final MipmapChain chain = MipmapChain.of(256, 16, true);
		Assert.assertEquals(128, chain.getWidth(1));
		Assert.assertEquals(8, chain.getHeight(1));
		Assert.assertEquals(1, chain.getWidth(8));
		Assert.assertEquals(1, chain.getHeight(8));
	}

	@Test
	public void roundsTheOddSizesDown() {
		final MipmapChain chain = MipmapChain.of(220, 3, true);
		Assert.assertEquals(8, chain.getLevels());
		Assert.assertEquals(Arrays.asList(220, 110, 55, 27, 13, 6, 3, 1), MipmapChainTest.widths(chain));
		Assert.assertEquals(1, chain.getHeight(1));
	}

	@Test
	public void copiesEachLevelFromThePreviousOne() {
		final List<String> steps = new ArrayList<>();
		MipmapChain.of(5, 3, true).forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> steps.add(level + ":" + sourceWidth + "x" + sourceHeight + ">" + targetWidth + "x" + targetHeight));
		Assert.assertEquals(Arrays.asList("1:5x3>2x1", "2:2x1>1x1"), steps);
	}

	@Test
	public void limitsItsLevels() {
		final MipmapChain chain = MipmapChain.of(256, 16, true).limit(5);
		Assert.assertEquals(5, chain.getLevels());
		Assert.assertEquals(16, chain.getWidth(4));
		Assert.assertEquals(1, chain.getHeight(4));
		Assert.assertEquals(1, MipmapChain.of(256, 16, true).limit(0).getLevels());
	}

	@Test
	public void keepsItsLevelsUnderAHigherLimit() {
		final MipmapChain chain = MipmapChain.of(256, 16, true);
		Assert.assertSame(chain, chain.limit(9));
		Assert.assertSame(chain, chain.limit(Integer.MAX_VALUE));
	}

	@Test
	public void copiesNothingWithoutMipmaps() {
		final List<Integer> levels = new ArrayList<>();
		MipmapChain.of(64, 64, false).forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> levels.add(level));
		Assert.assertTrue(levels.isEmpty());
	}

	private static List<Integer> widths(final MipmapChain chain) {
		final List<Integer> widthList = new ArrayList<>();
		for (int level = 0; level < chain.getLevels(); level++) {
			widthList.add(chain.getWidth(level));
		}
		return widthList;
	}

}