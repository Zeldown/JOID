package dev.joid.test.snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.asset.Asset;

public class SnapshotUrlAssetLocatorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File previous;

	@Before
	public void useATemporaryCache() {
		this.previous = JOIDEnv.getCacheDir();
		JOIDEnv.cacheDir(this.folder.getRoot());
	}

	@After
	public void restoreTheCache() {
		JOIDEnv.cacheDir(this.previous);
	}

	@Test
	public void locatesOnlyUrls() {
		final SnapshotUrlAssetLocator locator = new SnapshotUrlAssetLocator();
		Assert.assertTrue(locator.supports("https://example.invalid/image.png"));
		Assert.assertFalse(locator.supports(new File("image.png")));
	}

	@Test
	public void leavesTheOtherStringsToTheOtherLocators() {
		final SnapshotUrlAssetLocator locator = new SnapshotUrlAssetLocator();
		Assert.assertTrue(locator.supports("http://example.invalid/image.png"));
		Assert.assertFalse(locator.supports("assets/demo/image.png"));
		Assert.assertFalse(locator.supports("joid://image.png"));
	}

	@Test
	public void staysRemoteUntilCached() {
		final Asset asset = new SnapshotUrlAssetLocator().locate("https://example.invalid/image.png");
		Assert.assertEquals("https://example.invalid/image.png", asset.getUniqueId());
		Assert.assertTrue(asset.isRemote());
	}

	@Test
	public void readsTheCachedCopyNamedAfterTheHashOfTheUrl() throws IOException {
		final File directory = this.folder.newFolder("snapshot");
		Files.write(new File(directory, "e3bde048dd084a04010db6c257046213895b12f6").toPath(), "cached".getBytes(StandardCharsets.UTF_8));
		final Asset asset = new SnapshotUrlAssetLocator().locate("https://example.invalid/image.png");
		Assert.assertFalse(asset.isRemote());
		Assert.assertArrayEquals("cached".getBytes(StandardCharsets.UTF_8), asset.read());
	}

	@Test
	public void failsToDownloadAnUnknownProtocol() {
		final Asset asset = new SnapshotUrlAssetLocator().locate("joid://image.png");
		try {
			asset.open();
			Assert.fail();
		} catch (final IOException e) {
			Assert.assertArrayEquals(new String[0], new File(this.folder.getRoot(), "snapshot").list((parent, name) -> !name.startsWith(".")));
		}
	}

}