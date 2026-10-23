package dev.joid.lib.asset;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import lombok.NonNull;

public class AssetTest {

	private static final byte[] CONTENT = "joid asset layer".getBytes(StandardCharsets.UTF_8);

	private static File file;

	@Test
	public void peeksPastTheEndOfAFile() {
		Assert.assertEquals(AssetTest.CONTENT.length, Asset.of(AssetTest.file).peek(4096).length);
	}

	@Test
	public void identifiesAUrlWithoutOpeningIt() {
		final Asset asset = Asset.of("https://example.invalid/image.png");
		Assert.assertEquals("https://example.invalid/image.png", asset.getUniqueId());
	}

	@BeforeClass
	public static void write() throws IOException {
		AssetTest.file = File.createTempFile("joid-asset-", ".bin");
		AssetTest.file.deleteOnExit();
		Files.write(AssetTest.file.toPath(), AssetTest.CONTENT);
	}

	@AfterClass
	public static void delete() {
		Assert.assertTrue(AssetTest.file.delete());
	}

	@Test
	public void readsAFileAgain() throws IOException {
		final Asset asset = Asset.of(AssetTest.file);
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
	}

	@Test
	public void peeksWithoutConsumingAStream() throws IOException {
		final Asset asset = Asset.of(new ByteArrayInputStream(AssetTest.CONTENT));
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.peek(4));
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.peek(4));
		Assert.assertArrayEquals(AssetTest.CONTENT, asset.read());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("FileAsset[" + AssetTest.file.getAbsolutePath() + "]", Asset.of(AssetTest.file).toString());
		Assert.assertEquals("HandleAsset[handle]", new HandleAsset().toString());
	}

	@Test
	public void isLocalByDefault() {
		Assert.assertFalse(new HandleAsset().isRemote());
	}

	@Test
	public void peeksTheStartOfAnAsset() {
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), new HandleAsset().peek(4));
	}

	@Test
	public void peeksAcrossShortReads() {
		Assert.assertArrayEquals("joid asset".getBytes(StandardCharsets.UTF_8), new TricklingAsset().peek(10));
	}

	@Test(expected = UncheckedIOException.class)
	public void failsToPeekAnAssetThatCannotOpen() {
		new FailingAsset().peek(4);
	}

	@Test(expected = IOException.class)
	public void failsToReadAnAssetThatCannotOpen() throws IOException {
		new FailingAsset().read();
	}

	@Test
	public void readsPastItsBuffer() throws IOException {
		final byte[] content = new byte[20000];
		for (int i = 0; i < content.length; i++) {
			content[i] = (byte) i;
		}
		Assert.assertArrayEquals(content, Asset.of(new ByteArrayInputStream(content)).read());
	}

	@Test
	public void readsAnEmptyAsset() throws IOException {
		Assert.assertEquals(0, Asset.of(new ByteArrayInputStream(new byte[0])).read().length);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullHandle() {
		Asset.of(null);
	}

	private static final class HandleAsset extends Asset {

		private HandleAsset() {
			super("handle");
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(AssetTest.CONTENT);
		}

	}

	private static final class FailingAsset extends Asset {

		private FailingAsset() {
			super("failing");
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			throw new IOException("unreadable");
		}

	}

	private static final class TricklingAsset extends Asset {

		private TricklingAsset() {
			super("trickling");
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(AssetTest.CONTENT) {

				@Override
				public synchronized int read(final byte[] buffer, final int offset, final int length) {
					return super.read(buffer, offset, Math.min(1, length));
				}

			};
		}

	}

}