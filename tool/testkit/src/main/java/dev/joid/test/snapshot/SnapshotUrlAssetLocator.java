package dev.joid.test.snapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.locator.IAssetLocator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SnapshotUrlAssetLocator implements IAssetLocator {

	private final File directory;

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String;
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		final String url = (String) handle;
		return new CachedAsset(url, new File(this.directory, SnapshotUrlAssetLocator.hash(url)));
	}

	private static void download(final @NonNull String url, final @NonNull File file) {
		try {
			final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");

			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			try (InputStream stream = connection.getInputStream()) {
				final byte[] buffer = new byte[8192];
				for (int read = stream.read(buffer); read != -1; read = stream.read(buffer)) {
					output.write(buffer, 0, read);
				}
			}

			file.getParentFile().mkdirs();
			Files.write(file.toPath(), output.toByteArray());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String hash(final String url) {
		try {
			return String.format("%040x", new BigInteger(1, MessageDigest.getInstance("SHA-1").digest(url.getBytes(StandardCharsets.UTF_8))));
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final class CachedAsset extends Asset {

		private final File file;

		private CachedAsset(final @NonNull String url, final @NonNull File file) {
			super(url);
			this.file = file;
		}

		@Override
		public boolean isRemote() {
			return !this.file.exists();
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			if (!this.file.exists()) {
				SnapshotUrlAssetLocator.download(super.getUniqueId(), this.file);
			}
			return new FileInputStream(this.file);
		}

	}

}