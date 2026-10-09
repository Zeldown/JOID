package dev.joid.lib.asset.locator.impl;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.FileAsset;

public class FileAssetLocatorTest {

	@Test
	public void supportsOnlyFiles() {
		final FileAssetLocator locator = new FileAssetLocator();
		Assert.assertTrue(locator.supports(new File("image.png")));
		Assert.assertFalse(locator.supports("image.png"));
		Assert.assertFalse(locator.supports(new ByteArrayInputStream(new byte[0])));
	}

	@Test
	public void locatesAFileAsset() {
		final File file = new File("image.png");
		final Asset asset = new FileAssetLocator().locate(file);
		Assert.assertTrue(asset instanceof FileAsset);
		Assert.assertSame(file, ((FileAsset) asset).getFile());
	}

}