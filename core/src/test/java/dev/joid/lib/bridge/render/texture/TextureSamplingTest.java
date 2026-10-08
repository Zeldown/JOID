package dev.joid.lib.bridge.render.texture;

import org.junit.Assert;
import org.junit.Test;

public class TextureSamplingTest {

	@Test
	public void holdsEveryCombination() {
		Assert.assertEquals(TextureFilter.values().length * TextureWrap.values().length * 2, TextureSampling.values().size());
	}

	@Test
	public void indexesEachCombinationByItsPosition() {
		for (int i = 0; i < TextureSampling.values().size(); i++) {
			final TextureSampling sampling = TextureSampling.values().get(i);
			Assert.assertEquals(i, sampling.getIndex());
			Assert.assertSame(sampling, TextureSampling.of(sampling.getFilter(), sampling.getWrap(), sampling.isMipmapped()));
		}
	}

	@Test
	public void keepsWhatItIsGiven() {
		final TextureSampling sampling = TextureSampling.of(TextureFilter.LINEAR, TextureWrap.CLAMP_TO_BORDER, true);
		Assert.assertSame(TextureFilter.LINEAR, sampling.getFilter());
		Assert.assertSame(TextureWrap.CLAMP_TO_BORDER, sampling.getWrap());
		Assert.assertTrue(sampling.isMipmapped());
	}

	@Test
	public void filtersMipmapsOnlyWhenLinear() {
		Assert.assertTrue(TextureSampling.of(TextureFilter.LINEAR, TextureWrap.REPEAT, true).isMipmapFiltered());
		Assert.assertFalse(TextureSampling.of(TextureFilter.NEAREST, TextureWrap.REPEAT, true).isMipmapFiltered());
		Assert.assertFalse(TextureSampling.of(TextureFilter.LINEAR, TextureWrap.REPEAT, false).isMipmapFiltered());
	}

}