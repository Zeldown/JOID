package dev.joid.lib.asset.dto.locator.impl;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.UrlAsset;
import dev.joid.lib.asset.dto.locator.IAssetLocator;
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