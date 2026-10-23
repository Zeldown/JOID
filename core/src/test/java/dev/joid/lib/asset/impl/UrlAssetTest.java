package dev.joid.lib.asset.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.net.ssl.SSLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UrlAssetTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private Server server;

	@Before
	public void startTheServer() throws IOException {
		this.server = new Server();
	}

	@After
	public void stopTheServer() throws IOException {
		this.server.socket.close();
	}

	@Test
	public void isARemoteAsset() {
		final UrlAsset asset = UrlAsset.of("https://example.invalid/image.png");
		Assert.assertEquals("https://example.invalid/image.png", asset.getUrl());
		Assert.assertEquals("https://example.invalid/image.png", asset.getUniqueId());
		Assert.assertTrue(asset.isRemote());
		Assert.assertNull(asset.getFile());
	}

	@Test
	public void downloadsItsContent() throws IOException {
		Assert.assertArrayEquals("joid remote asset".getBytes(StandardCharsets.UTF_8), UrlAsset.of(this.server.url("http", "/image.png")).read());
		Assert.assertEquals(Collections.singletonList("/image.png"), this.server.paths);
	}

	@Test
	public void introducesItselfAsABrowser() throws IOException {
		UrlAsset.of(this.server.url("http", "/image.png")).read();
		Assert.assertTrue(this.server.agents.get(0), this.server.agents.get(0).startsWith("Mozilla/5.0"));
	}

	@Test
	public void fallsBackToPlainHttp() throws IOException {
		Assert.assertArrayEquals("joid remote asset".getBytes(StandardCharsets.UTF_8), UrlAsset.of(this.server.url("https", "/image.png")).read());
		Assert.assertEquals(Collections.singletonList("/image.png"), this.server.paths);
	}

	@Test
	public void readsAFileUrl() throws IOException {
		final File file = this.folder.newFile("image.png");
		Files.write(file.toPath(), "joid file asset".getBytes(StandardCharsets.UTF_8));
		final UrlAsset asset = UrlAsset.of(file.toURI().toString());
		Assert.assertArrayEquals("joid file asset".getBytes(StandardCharsets.UTF_8), asset.read());
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.peek(4));
		Assert.assertEquals(file.getAbsoluteFile(), asset.getFile().getAbsoluteFile());
		Assert.assertFalse(asset.isRemote());
	}

	@Test
	public void readsAFileUrlWithASpaceThroughAStream() throws IOException {
		final File file = this.folder.newFile("my image.png");
		Files.write(file.toPath(), "joid file asset".getBytes(StandardCharsets.UTF_8));
		final UrlAsset asset = UrlAsset.of("file:" + file.getAbsolutePath().replace(File.separatorChar, '/'));
		Assert.assertNull(asset.getFile());
		Assert.assertArrayEquals("joid file asset".getBytes(StandardCharsets.UTF_8), asset.read());
	}

	@Test
	public void keepsAFailureThatIsNotSecureWithoutFallback() throws IOException {
		final int port;
		try (final ServerSocket closed = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
			port = closed.getLocalPort();
		}

		try {
			UrlAsset.of("https://" + InetAddress.getLoopbackAddress().getHostAddress() + ":" + port + "/image.png").read();
			Assert.fail("A closed port must fail");
		} catch (final IOException expected) {
			Assert.assertFalse(expected instanceof SSLException);
			Assert.assertEquals(0, expected.getSuppressed().length);
		}
	}

	@Test(timeout = 60000L, expected = SocketTimeoutException.class)
	public void givesUpOnAServerThatNeverAnswers() throws IOException {
		try (final ServerSocket mute = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
			UrlAsset.of("http://127.0.0.1:" + mute.getLocalPort() + "/image.png").read();
		}
	}

	@Test
	public void readsAJarUrl() throws IOException {
		final File file = this.folder.newFile("assets.jar");
		try (final JarOutputStream output = new JarOutputStream(new FileOutputStream(file))) {
			output.putNextEntry(new ZipEntry("image.png"));
			output.write("joid jar asset".getBytes(StandardCharsets.UTF_8));
			output.closeEntry();
		}
		final UrlAsset asset = UrlAsset.of("jar:" + file.toURI() + "!/image.png");
		try {
			Assert.assertArrayEquals("joid jar asset".getBytes(StandardCharsets.UTF_8), asset.read());
			Assert.assertNull(asset.getFile());
			Assert.assertFalse(asset.isRemote());
		} finally {
			((JarURLConnection) new URL(asset.getUrl()).openConnection()).getJarFile().close();
		}
	}

	@Test(expected = UncheckedIOException.class)
	public void failsToPeekAMissingFileUrl() {
		UrlAsset.of(new File(this.folder.getRoot(), "missing.png").toURI().toString()).peek(4);
	}

	@Test
	public void requestsAMissingPlainHttpAssetOnce() {
		try {
			UrlAsset.of(this.server.url("http", "/missing.png")).read();
			Assert.fail("A missing asset must fail");
		} catch (final IOException expected) {
			Assert.assertEquals(Collections.singletonList("/missing.png"), this.server.paths);
		}
	}

	@Test
	public void peeksTheStartOfItsContent() {
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), UrlAsset.of(this.server.url("http", "/image.png")).peek(4));
	}

	@Test(expected = FileNotFoundException.class)
	public void failsToReadAMissingAsset() throws IOException {
		UrlAsset.of(this.server.url("http", "/missing.png")).read();
	}

	@Test(expected = UncheckedIOException.class)
	public void failsToPeekAMissingAsset() {
		UrlAsset.of(this.server.url("http", "/missing.png")).peek(4);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullUrl() {
		UrlAsset.of(null);
	}

	@Test
	public void reportsTheSecureFailureWhenThePlainFallbackFailsToo() {
		try {
			UrlAsset.of(this.server.url("https", "/missing.png")).read();
			Assert.fail("A missing asset must fail");
		} catch (final IOException expected) {
			Assert.assertFalse(expected instanceof FileNotFoundException);
			Assert.assertTrue(Arrays.stream(expected.getSuppressed()).anyMatch(FileNotFoundException.class::isInstance));
		}
	}

	@Test
	public void fallsBackWithoutTouchingThePath() throws IOException {
		UrlAsset.of(this.server.url("https", "/https/image.png")).read();
		Assert.assertEquals(Collections.singletonList("/https/image.png"), this.server.paths);
	}

	private static final class Server implements Runnable {

		private final List<String> paths  = new CopyOnWriteArrayList<>();
		private final ServerSocket socket;
		private final List<String> agents = new CopyOnWriteArrayList<>();

		private Server() throws IOException {
			this.socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
			final Thread thread = new Thread(this, "UrlAssetTest");
			thread.setDaemon(true);
			thread.start();
		}

		@Override
		public void run() {
			while (!this.socket.isClosed()) {
				try (final Socket client = this.socket.accept()) {
					this.answer(client);
				} catch (final IOException closed) {}
			}
		}

		private String url(final String protocol, final String path) {
			return protocol + "://127.0.0.1:" + this.socket.getLocalPort() + path;
		}

		private void answer(final Socket client) throws IOException {
			final InputStream input = client.getInputStream();
			if (input.read() == 0x16) {
				return;
			}

			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.ISO_8859_1))) {
				final String path = reader.readLine().split(" ")[1];
				String header;
				while ((header = reader.readLine()) != null && !header.isEmpty()) {
					if (header.startsWith("User-Agent: ")) {
						this.agents.add(header.substring(12));
					}
				}
				this.paths.add(path);

				final byte[] body = path.contains("missing") ? new byte[0] : "joid remote asset".getBytes(StandardCharsets.UTF_8);
				final String status = path.contains("missing") ? "404 Not Found" : "200 OK";
				final OutputStream output = client.getOutputStream();
				output.write(("HTTP/1.1 " + status + "\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
				output.write(body);
				output.flush();
			}
		}

	}

}