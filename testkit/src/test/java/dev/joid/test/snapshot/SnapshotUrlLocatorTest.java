package dev.joid.test.snapshot;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.asset.Asset;

public class SnapshotUrlLocatorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void locatesOnlyUrls() {
		final SnapshotUrlLocator locator = new SnapshotUrlLocator(this.folder.getRoot());
		Assert.assertTrue(locator.supports("https://example.invalid/image.png"));
		Assert.assertFalse(locator.supports(new File("image.png")));
	}

	@Test
	public void staysRemoteUntilCached() {
		final Asset asset = new SnapshotUrlLocator(this.folder.getRoot()).locate("https://example.invalid/image.png");
		Assert.assertEquals("https://example.invalid/image.png", asset.getUniqueId());
		Assert.assertTrue(asset.isRemote());
	}

	@Test
	public void readsTheCachedCopyNamedAfterTheHashOfTheUrl() throws IOException {
		Files.write(new File(this.folder.getRoot(), "e3bde048dd084a04010db6c257046213895b12f6").toPath(), "cached".getBytes(StandardCharsets.UTF_8));
		final Asset asset = new SnapshotUrlLocator(this.folder.getRoot()).locate("https://example.invalid/image.png");
		Assert.assertFalse(asset.isRemote());
		Assert.assertArrayEquals("cached".getBytes(StandardCharsets.UTF_8), asset.read());
	}

	@Test
	public void failsToDownloadAnUnknownProtocol() throws IOException {
		final Asset asset = new SnapshotUrlLocator(this.folder.getRoot()).locate("joid://image.png");
		try {
			asset.open();
			Assert.fail();
		} catch (final UncheckedIOException e) {
			Assert.assertEquals(0, this.folder.getRoot().list().length);
		}
	}

}