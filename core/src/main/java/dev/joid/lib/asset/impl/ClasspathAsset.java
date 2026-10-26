package dev.joid.lib.asset.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import dev.joid.lib.asset.Asset;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ClasspathAsset extends Asset {

	private final URL    url;
	private final String path;

	private ClasspathAsset(final String path, final URL url) {
		super("classpath:" + path);
		this.path = path;
		this.url = url;
	}

	public static @NonNull ClasspathAsset of(final @NonNull String path, final @NonNull URL url) {
		return new ClasspathAsset(path, url);
	}

	@Override
	public File getFile() {
		if (!"file".equals(this.url.getProtocol())) {
			return null;
		}

		try {
			return new File(this.url.toURI());
		} catch (final URISyntaxException exception) {
			return null;
		}
	}

	@Override
	public @NonNull InputStream open() throws IOException {
		return this.url.openStream();
	}

}