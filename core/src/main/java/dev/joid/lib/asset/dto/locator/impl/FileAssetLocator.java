package dev.joid.lib.asset.dto.locator.impl;

import java.io.File;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.FileAsset;
import dev.joid.lib.asset.dto.locator.IAssetLocator;
import lombok.NonNull;

public class FileAssetLocator implements IAssetLocator {

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof File;
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		return FileAsset.create((File) handle);
	}

}