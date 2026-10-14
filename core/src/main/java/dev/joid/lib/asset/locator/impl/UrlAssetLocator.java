package dev.joid.lib.asset.locator.impl;

import java.util.regex.Pattern;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.UrlAsset;
import dev.joid.lib.asset.locator.IAssetLocator;
import lombok.NonNull;

public class UrlAssetLocator implements IAssetLocator {

	private static final Pattern SCHEME = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]+:");

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String && UrlAssetLocator.SCHEME.matcher((String) handle).find();
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		return UrlAsset.create((String) handle);
	}

}