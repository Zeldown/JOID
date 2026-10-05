package dev.joid.lib.asset.dto.locator.impl;

import java.io.InputStream;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.StreamAsset;
import dev.joid.lib.asset.dto.locator.IAssetLocator;
import lombok.NonNull;

public class StreamAssetLocator implements IAssetLocator {

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof InputStream;
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		return StreamAsset.create((InputStream) handle);
	}

}