package dev.joid.lib.asset.locator.impl;

import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.ClasspathAsset;
import dev.joid.lib.asset.impl.FileAsset;
import dev.joid.lib.asset.locator.IAssetLocator;
import lombok.NonNull;

public class PathAssetLocator implements IAssetLocator {

	private static final Pattern SCHEME = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]+:");

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String && !PathAssetLocator.SCHEME.matcher((String) handle).find();
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		final String path = (String) handle;
		final String name = path.startsWith("/") ? path.substring(1) : path;
		final URL url = PathAssetLocator.find(name);
		if (url != null) {
			return ClasspathAsset.create(name, url);
		}

		final File file = new File(path);
		if (file.isFile()) {
			return FileAsset.create(file);
		}
		throw new IllegalArgumentException("The asset " + path + " is not found in the classpath nor as a file, check the path or give a URL with its scheme (https:, file:, jar:)");
	}

	private static URL find(final String name) {
		final ClassLoader context = Thread.currentThread().getContextClassLoader();
		final URL url = context == null ? null : context.getResource(name);
		return url != null ? url : PathAssetLocator.class.getClassLoader().getResource(name);
	}

}