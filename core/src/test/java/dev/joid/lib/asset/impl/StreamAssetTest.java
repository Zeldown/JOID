package dev.joid.lib.asset.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class StreamAssetTest {

	private static final byte[] CONTENT = "joid stream asset".getBytes(StandardCharsets.UTF_8);

	@Test
	public void keepsABufferedStream() {
		final BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		Assert.assertSame(stream, StreamAsset.create(stream).open());
	}

	@Test
	public void buffersARawStream() throws IOException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(StreamAssetTest.CONTENT);
		final StreamAsset asset = StreamAsset.create(stream);
		Assert.assertTrue(asset.open() instanceof BufferedInputStream);
		Assert.assertNotSame(stream, asset.open());
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
	}

	@Test
	public void isIdentifiedByItsStream() {
		final ByteArrayInputStream stream = new ByteArrayInputStream(StreamAssetTest.CONTENT);
		Assert.assertEquals(stream.toString(), StreamAsset.create(stream).getUniqueId());
	}

	@Test
	public void isLocal() {
		Assert.assertFalse(StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT)).isRemote());
	}

	@Test
	public void peeksNothingOnceRead() throws IOException {
		final StreamAsset asset = StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
		Assert.assertEquals(0, asset.peek(4).length);
	}

	@Test(expected = IOException.class)
	public void cannotBeReadTwice() throws IOException {
		final StreamAsset asset = StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		asset.read();
		asset.read();
	}

	@Test
	public void peeksPastTheEndOfItsStream() throws IOException {
		final StreamAsset asset = StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.peek(4096));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
	}

	@Test
	public void peeksNothingFromAFailingStream() {
		Assert.assertEquals(0, StreamAsset.create(new FailingStream()).peek(4).length);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullStream() {
		StreamAsset.create(null);
	}

	private static final class FailingStream extends InputStream {

		@Override
		public int read() throws IOException {
			throw new IOException("unreadable");
		}

	}

}