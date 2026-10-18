package dev.joid.lib.asset.locator.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.ClasspathAsset;
import dev.joid.lib.asset.impl.FileAsset;
import dev.joid.lib.asset.impl.UrlAsset;

public class StringAssetLocatorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void supportsOnlyStrings() {
		final StringAssetLocator locator = new StringAssetLocator();
		Assert.assertTrue(locator.supports("assets/image.png"));
		Assert.assertTrue(locator.supports("https://example.invalid/image.png"));
		Assert.assertFalse(locator.supports(new File("image.png")));
		Assert.assertFalse(locator.supports(new ByteArrayInputStream(new byte[0])));
	}

	@Test
	public void locatesAStringWithASchemeAsAUrl() {
		final StringAssetLocator locator = new StringAssetLocator();
		Assert.assertEquals("https://example.invalid/image.png", ((UrlAsset) locator.locate("https://example.invalid/image.png")).getUrl());
		Assert.assertEquals("file:/home/image.png", ((UrlAsset) locator.locate("file:/home/image.png")).getUrl());
		Assert.assertEquals("jar:file:/home/assets.jar!/image.png", ((UrlAsset) locator.locate("jar:file:/home/assets.jar!/image.png")).getUrl());
	}

	@Test
	public void findsAPathInTheClasspathFirst() throws IOException {
		final Asset asset = new StringAssetLocator().locate("animation/still.png");
		Assert.assertTrue(asset instanceof ClasspathAsset);
		Assert.assertEquals("classpath:animation/still.png", asset.getUniqueId());
		Assert.assertFalse(asset.isRemote());
		Assert.assertArrayEquals(Asset.of(StringAssetLocatorTest.class.getResourceAsStream("/animation/still.png")).read(), asset.read());
	}

	@Test
	public void readsAClasspathPathWithALeadingSlash() {
		Assert.assertEquals("classpath:animation/still.png", new StringAssetLocator().locate("/animation/still.png").getUniqueId());
	}

	@Test
	public void givesTheFileOfAClasspathDirectory() {
		final File file = new StringAssetLocator().locate("animation/still.png").getFile();
		Assert.assertNotNull(file);
		Assert.assertTrue(file.isFile());
	}

	@Test
	public void findsAPathAsAFileThen() throws IOException {
		final File file = this.folder.newFile("image.png");
		Files.write(file.toPath(), "joid path asset".getBytes(StandardCharsets.UTF_8));
		final Asset asset = new StringAssetLocator().locate(file.getAbsolutePath());
		Assert.assertTrue(asset instanceof FileAsset);
		Assert.assertEquals(file.getAbsoluteFile(), asset.getFile().getAbsoluteFile());
		Assert.assertArrayEquals("joid path asset".getBytes(StandardCharsets.UTF_8), asset.read());
	}

	@Test
	public void namesAPathFoundNowhere() {
		try {
			new StringAssetLocator().locate("assets/missing.png");
			Assert.fail("A missing path must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The asset assets/missing.png is not found in the classpath nor as a file, check the path or give a URL with its scheme (https:, file:, jar:)", expected.getMessage());
		}
	}

}