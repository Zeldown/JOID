package dev.joid.lib.asset.dto.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UrlAssetTest {

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
		final UrlAsset asset = UrlAsset.create("https://example.invalid/image.png");
		Assert.assertEquals("https://example.invalid/image.png", asset.getUrl());
		Assert.assertEquals("https://example.invalid/image.png", asset.getUniqueId());
		Assert.assertTrue(asset.isRemote());
		Assert.assertTrue(asset.isReopenable());
	}

	@Test
	public void downloadsItsContent() throws IOException {
		Assert.assertArrayEquals("joid remote asset".getBytes(StandardCharsets.UTF_8), UrlAsset.create(this.server.url("http", "/image.png")).read());
		Assert.assertEquals(Collections.singletonList("/image.png"), this.server.paths);
	}

	@Test
	public void introducesItselfAsABrowser() throws IOException {
		UrlAsset.create(this.server.url("http", "/image.png")).read();
		Assert.assertTrue(this.server.agents.get(0), this.server.agents.get(0).startsWith("Mozilla/5.0"));
	}

	@Test
	public void fallsBackToPlainHttp() throws IOException {
		Assert.assertArrayEquals("joid remote asset".getBytes(StandardCharsets.UTF_8), UrlAsset.create(this.server.url("https", "/image.png")).read());
		Assert.assertEquals(Collections.singletonList("/image.png"), this.server.paths);
	}

	@Test
	public void peeksTheStartOfItsContent() {
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), UrlAsset.create(this.server.url("http", "/image.png")).peek(4));
	}

	@Test(expected = FileNotFoundException.class)
	public void failsToReadAMissingAsset() throws IOException {
		UrlAsset.create(this.server.url("http", "/missing.png")).read();
	}

	@Test
	public void peeksNothingFromAMissingAsset() {
		Assert.assertEquals(0, UrlAsset.create(this.server.url("http", "/missing.png")).peek(4).length);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullUrl() {
		UrlAsset.create(null);
	}

	@Test
	public void fallsBackWithoutTouchingThePath() throws IOException {
		UrlAsset.create(this.server.url("https", "/https/image.png")).read();
		Assert.assertEquals(Collections.singletonList("/https/image.png"), this.server.paths);
	}

	private static final class Server implements Runnable {

		private final ServerSocket socket;
		private final List<String> paths  = new CopyOnWriteArrayList<>();
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
				try (Socket client = this.socket.accept()) {
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

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.ISO_8859_1))) {
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