package dev.joid.lib.asset.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import dev.joid.lib.asset.Asset;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UrlAsset extends Asset {

	private final String url;

	private UrlAsset(final @NonNull String url) {
		super(url);
		this.url = url;
	}

	public static @NonNull UrlAsset create(final @NonNull String url) {
		return new UrlAsset(url);
	}

	@Override
	public File getFile() {
		return this.url.startsWith("file:") ? new File(URI.create(this.url)) : null;
	}

	@Override
	public boolean isRemote() {
		return !this.url.startsWith("file:") && !this.url.startsWith("jar:");
	}

	@Override
	public @NonNull InputStream open() throws IOException {
		try {
			return UrlAsset.connect(this.url);
		} catch (final IOException exception) {
			if (!this.url.startsWith("https:")) {
				throw exception;
			}

			try {
				return UrlAsset.connect(this.url.replaceFirst("^https:", "http:"));
			} catch (final IOException fallback) {
				exception.addSuppressed(fallback);
				throw exception;
			}
		}
	}

	private static @NonNull InputStream connect(final @NonNull String url) throws IOException {
		final URLConnection connection = new URL(url).openConnection();
		if (connection instanceof HttpURLConnection) {
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
		}

		return connection.getInputStream();
	}

}