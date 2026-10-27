package com.example.further;

import java.net.URL;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.ClasspathAsset;
import dev.joid.lib.asset.locator.IAssetLocator;

// #region locator
public class FormatsPackAssetLocator implements IAssetLocator {

	@Override
	public boolean supports(final Object handle) {
		return handle instanceof String && ((String) handle).startsWith("pack:");
	}

	@Override
	public Asset locate(final Object handle) {
		final String path = "formats/" + ((String) handle).substring(5);
		final URL url = FormatsPackAssetLocator.class.getClassLoader().getResource(path);
		if (url == null) {
			throw new IllegalArgumentException("The asset " + handle + " is not in the pack, check its path");
		}
		return ClasspathAsset.of(path, url);
	}

}
// #endregion