package dev.joid.lib.asset.locator;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.locator.impl.FileAssetLocator;
import dev.joid.lib.asset.locator.impl.PathAssetLocator;
import dev.joid.lib.asset.locator.impl.StreamAssetLocator;
import dev.joid.lib.asset.locator.impl.UrlAssetLocator;
import dev.joid.lib.utils.registry.Registry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetLocator {

	private static final Registry<IAssetLocator> LOCATORS = Registry.create();

	static {
		AssetLocator.register(new UrlAssetLocator());
		AssetLocator.register(new PathAssetLocator());
		AssetLocator.register(new FileAssetLocator());
		AssetLocator.register(new StreamAssetLocator());
	}

	public static void register(final @NonNull IAssetLocator locator) {
		AssetLocator.LOCATORS.register(locator);
	}

	public static void unregister(final @NonNull IAssetLocator locator) {
		AssetLocator.LOCATORS.unregister(locator);
	}

	public static boolean supports(final @NonNull Object handle) {
		return handle instanceof Asset || AssetLocator.LOCATORS.find(locator -> locator.supports(handle)) != null;
	}

	public static @NonNull Asset locate(final @NonNull Object handle) {
		if (handle instanceof Asset) {
			return (Asset) handle;
		}

		final IAssetLocator locator = AssetLocator.LOCATORS.find(candidate -> candidate.supports(handle));
		if (locator == null) {
			throw new IllegalArgumentException("No asset locator knows the input " + handle + " of type " + handle.getClass().getName() + ", register an IAssetLocator that supports it with AssetLocator.register(...)");
		}

		return locator.locate(handle);
	}

}