package dev.joid.demo.asset.locator.impl;

import java.net.URL;

import dev.joid.lib.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.ClasspathAsset;
import dev.joid.lib.asset.locator.IAssetLocator;
import lombok.NonNull;

public final class DemoAssetLocator implements IAssetLocator {

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String && ((String) handle).startsWith("demo:");
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		final String path = "assets/demo/" + ((String) handle).substring(5);
		final URL url = JOID.class.getResource("/" + path);
		if (url == null) {
			throw new IllegalArgumentException("The asset " + handle + " is not in the demo assets, check its path");
		}
		return ClasspathAsset.of(path, url);
	}

}