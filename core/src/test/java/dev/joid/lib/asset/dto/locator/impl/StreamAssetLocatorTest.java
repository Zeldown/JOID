package dev.joid.lib.asset.dto.locator.impl;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.StreamAsset;

public class StreamAssetLocatorTest {

	@Test
	public void supportsOnlyStreams() {
		final StreamAssetLocator locator = new StreamAssetLocator();
		Assert.assertTrue(locator.supports(new ByteArrayInputStream(new byte[0])));
		Assert.assertFalse(locator.supports(new File("image.png")));
		Assert.assertFalse(locator.supports("image.png"));
	}

	@Test
	public void locatesAStreamAsset() {
		final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
		final Asset asset = new StreamAssetLocator().locate(stream);
		Assert.assertTrue(asset instanceof StreamAsset);
		Assert.assertEquals(stream.toString(), asset.getUniqueId());
	}

}