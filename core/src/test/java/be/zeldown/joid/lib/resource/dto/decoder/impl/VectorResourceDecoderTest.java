package be.zeldown.joid.lib.resource.dto.decoder.impl;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.ResourceData;

public class VectorResourceDecoderTest {

	@Test
	public void rendersTheDocumentAtItsIntrinsicSize() {
		final VectorResourceDecoder decoder = new VectorResourceDecoder(Asset.of(VectorResourceDecoderTest.class.getResourceAsStream("/vector/icon.svg")));
		final ResourceData data = new ResourceData("icon.svg", null);
		decoder.decode(data);
		Assert.assertEquals(24, data.getWidth());
		Assert.assertEquals(24, data.getHeight());
		final int[] pixels = data.getData()[0];
		Assert.assertEquals(0, pixels[0] >>> 24);
		Assert.assertEquals(255, pixels[12 * 24 + 4] >>> 24);
		Assert.assertEquals(255, pixels[12 * 24 + 12] >>> 24);
	}

}