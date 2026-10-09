package dev.joid.lib.asset.locator.impl;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.UrlAsset;
import dev.joid.lib.asset.locator.IAssetLocator;
import lombok.NonNull;

public class UrlAssetLocator implements IAssetLocator {

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String;
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		return UrlAsset.create((String) handle);
	}

}