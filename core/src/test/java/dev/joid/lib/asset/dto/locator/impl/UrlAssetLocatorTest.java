package dev.joid.lib.asset.dto.locator.impl;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.UrlAsset;

public class UrlAssetLocatorTest {

	@Test
	public void supportsOnlyStrings() {
		final UrlAssetLocator locator = new UrlAssetLocator();
		Assert.assertTrue(locator.supports("https://example.invalid/image.png"));
		Assert.assertFalse(locator.supports(new File("image.png")));
		Assert.assertFalse(locator.supports(new ByteArrayInputStream(new byte[0])));
	}

	@Test
	public void locatesAUrlAsset() {
		final Asset asset = new UrlAssetLocator().locate("https://example.invalid/image.png");
		Assert.assertTrue(asset instanceof UrlAsset);
		Assert.assertEquals("https://example.invalid/image.png", ((UrlAsset) asset).getUrl());
	}

}