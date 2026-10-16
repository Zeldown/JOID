package dev.joid.lib.asset.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class StreamAssetTest {

	private static final byte[] CONTENT = "joid stream asset".getBytes(StandardCharsets.UTF_8);

	@Test
	public void opensANewStreamEveryTime() throws IOException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(StreamAssetTest.CONTENT);
		final StreamAsset asset = StreamAsset.create(stream);
		Assert.assertNotSame(asset.open(), asset.open());
		Assert.assertNotSame(stream, asset.open());
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
	}

	@Test
	public void givesEachStreamItsOwnId() {
		final String first = StreamAsset.create(new NamedStream()).getUniqueId();
		final String second = StreamAsset.create(new NamedStream()).getUniqueId();
		Assert.assertNotEquals(first, second);
		Assert.assertTrue(first, first.startsWith("stream_"));
	}

	@Test
	public void isLocal() {
		Assert.assertFalse(StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT)).isRemote());
	}

	@Test
	public void peeksAgainOnceRead() throws IOException {
		final StreamAsset asset = StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.peek(4));
	}

	@Test
	public void canBeReadTwice() throws IOException {
		final StreamAsset asset = StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
	}

	@Test
	public void peeksPastTheEndOfItsStream() throws IOException {
		final StreamAsset asset = StreamAsset.create(new ByteArrayInputStream(StreamAssetTest.CONTENT));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.peek(4096));
		Assert.assertArrayEquals(StreamAssetTest.CONTENT, asset.read());
	}

	@Test(expected = UncheckedIOException.class)
	public void failsToPeekAFailingStream() {
		StreamAsset.create(new FailingStream()).peek(4);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullStream() {
		StreamAsset.create(null);
	}

	private static final class NamedStream extends ByteArrayInputStream {

		private NamedStream() {
			super(StreamAssetTest.CONTENT);
		}

		@Override
		public String toString() {
			return "stream";
		}

	}

	private static final class FailingStream extends InputStream {

		@Override
		public int read() throws IOException {
			throw new IOException("unreadable");
		}

	}

}