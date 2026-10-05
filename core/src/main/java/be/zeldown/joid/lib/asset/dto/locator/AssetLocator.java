package be.zeldown.joid.lib.asset.dto.locator;

import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.asset.dto.locator.impl.FileAssetLocator;
import be.zeldown.joid.lib.asset.dto.locator.impl.StreamAssetLocator;
import be.zeldown.joid.lib.asset.dto.locator.impl.UrlAssetLocator;
import lombok.NonNull;

public class AssetLocator {

	private static final List<IAssetLocator> LOCATORS = new LinkedList<>();

	static {
		AssetLocator.register(new UrlAssetLocator());
		AssetLocator.register(new FileAssetLocator());
		AssetLocator.register(new StreamAssetLocator());
	}

	public static void register(final @NonNull IAssetLocator locator) {
		AssetLocator.LOCATORS.add(0, locator);
	}

	public static boolean supports(final @NonNull Object handle) {
		if (handle instanceof Asset) {
			return true;
		}

		for (final IAssetLocator locator : AssetLocator.LOCATORS) {
			if (locator.supports(handle)) {
				return true;
			}
		}

		return false;
	}

	public static @NonNull Asset locate(final @NonNull Object handle) {
		if (handle instanceof Asset) {
			return (Asset) handle;
		}

		for (final IAssetLocator locator : AssetLocator.LOCATORS) {
			if (locator.supports(handle)) {
				return locator.locate(handle);
			}
		}

		throw new IllegalArgumentException("No asset locator found for input of type " + handle.getClass().getName());
	}

}