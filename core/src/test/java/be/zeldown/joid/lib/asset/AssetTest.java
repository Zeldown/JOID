package be.zeldown.joid.lib.asset;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import be.zeldown.joid.lib.asset.dto.impl.FileAsset;
import be.zeldown.joid.lib.asset.dto.impl.StreamAsset;
import be.zeldown.joid.lib.asset.dto.impl.UrlAsset;
import be.zeldown.joid.lib.asset.dto.locator.AssetLocator;
import be.zeldown.joid.lib.asset.dto.locator.IAssetLocator;
import lombok.NonNull;

public class AssetTest {

	private static final byte[] CONTENT = "joid asset layer".getBytes(StandardCharsets.UTF_8);

	private static File file;

	@Test
	public void keepsAnAssetAsIs() {
		final Asset asset = Asset.of(AssetTest.file);
		Assert.assertSame(asset, Asset.of(asset));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnUnknownHandle() {
		Asset.of(new Object());
	}

	@Test
	public void peeksPastTheEndOfAFile() {
		Assert.assertEquals(AssetTest.CONTENT.length, Asset.of(AssetTest.file).peek(4096).length);
	}

	@Test
	public void locatesAUrlWithoutOpeningIt() {
		final Asset asset = Asset.of("https://example.invalid/image.png");
		Assert.assertTrue(asset instanceof UrlAsset);
		Assert.assertEquals("https://example.invalid/image.png", asset.getUniqueId());
	}

	@BeforeClass
	public static void write() throws IOException {
		AssetTest.file = File.createTempFile("joid-asset-", ".bin");
		AssetTest.file.deleteOnExit();
		Files.write(AssetTest.file.toPath(), AssetTest.CONTENT);
	}

	@Test
	public void locatesAFile() throws IOException {
		final Asset asset = Asset.of(AssetTest.file);
		Assert.assertTrue(asset instanceof FileAsset);
		Assert.assertEquals(AssetTest.file.getAbsolutePath(), asset.getUniqueId());
		Assert.assertTrue(asset.isReopenable());
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
	}

	@Test
	public void locatesAStream() throws IOException {
		final Asset asset = Asset.of(new ByteArrayInputStream(AssetTest.CONTENT));
		Assert.assertTrue(asset instanceof StreamAsset);
		Assert.assertFalse(asset.isReopenable());
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
	}

	@Test
	public void registersACustomLocator() throws IOException {
		AssetLocator.register(new HandleLocator());

		final Asset asset = Asset.of(new Handle());
		Assert.assertEquals("handle", asset.getUniqueId());
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
	}

	@Test
	public void peeksWithoutConsumingAStream() throws IOException {
		final Asset asset = Asset.of(new ByteArrayInputStream(AssetTest.CONTENT));
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.peek(4));
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.peek(4));
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
	}

	private static final class Handle {}

	private static final class HandleAsset extends Asset {

		private HandleAsset() {
			super("handle");
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(AssetTest.CONTENT);
		}

	}

	private static final class HandleLocator implements IAssetLocator {

		@Override
		public boolean supports(final @NonNull Object handle) {
			return handle instanceof Handle;
		}

		@Override
		public @NonNull Asset locate(final @NonNull Object handle) {
			return new HandleAsset();
		}

	}

}