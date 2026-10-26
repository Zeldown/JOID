package dev.joid.test.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.UrlAsset;
import dev.joid.lib.asset.locator.IAssetLocator;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.cache.FileCacheArea;
import lombok.NonNull;

public final class SnapshotUrlAssetLocator implements IAssetLocator {

	private static final FileCacheArea AREA = FileCache.area("snapshot").retention(Duration.ofDays(30L));

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String && (((String) handle).startsWith("http://") || ((String) handle).startsWith("https://"));
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		final String url = (String) handle;
		return new CachedAsset(url, SnapshotUrlAssetLocator.hash(url));
	}

	private static void download(final String url, final File file) throws IOException {
		try (final InputStream stream = UrlAsset.of(url).open()) {
			Files.copy(stream, file.toPath());
		}
	}

	private static String hash(final String url) {
		try {
			return String.format(Locale.ROOT, "%040x", new BigInteger(1, MessageDigest.getInstance("SHA-1").digest(url.getBytes(StandardCharsets.UTF_8))));
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException("The snapshot cache cannot name the download of " + url + ": this JVM has no SHA-1 digest, run the snapshots on a standard JVM", e);
		}
	}

	private static final class CachedAsset extends Asset {

		private final String key;

		private CachedAsset(final String url, final String key) {
			super(url);
			this.key = key;
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			return new FileInputStream(SnapshotUrlAssetLocator.AREA.compute(this.key, file -> SnapshotUrlAssetLocator.download(super.getUniqueId(), file)));
		}

		@Override
		public boolean isRemote() {
			return SnapshotUrlAssetLocator.AREA.entry(this.key) == null;
		}

	}

}