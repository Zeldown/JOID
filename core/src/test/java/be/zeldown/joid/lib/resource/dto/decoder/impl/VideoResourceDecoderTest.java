package be.zeldown.joid.lib.resource.dto.decoder.impl;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.ResourceData;

public class VideoResourceDecoderTest {

	@Test
	public void keepsTheTransparencyOfAWebm() {
		for (final String name : new String[] {"alpha-vp9.webm", "alpha-vp8.webm"}) {
			final VideoResourceDecoder decoder = new VideoResourceDecoder(Asset.of(VideoResourceDecoderTest.class.getResourceAsStream("/video/" + name)));
			final ResourceData data = new ResourceData(name, null);
			decoder.decode(data);
			try {
				final int[] pixels = data.getData()[0];
				Assert.assertEquals(64, data.getWidth());
				Assert.assertEquals(32, data.getHeight());
				Assert.assertEquals(name, 255, pixels[16 * 64 + 4] >>> 24);
				Assert.assertEquals(name, 0, pixels[16 * 64 + 50] >>> 24);
				Assert.assertEquals(name, 255, pixels[16 * 64 + 4] >> 16 & 0xFF, 8);
			} finally {
				decoder.release();
			}
		}
	}

}