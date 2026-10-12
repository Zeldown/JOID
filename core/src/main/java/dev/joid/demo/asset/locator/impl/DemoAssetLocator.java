package dev.joid.demo.asset.locator.impl;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.StreamAsset;
import dev.joid.lib.asset.locator.IAssetLocator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoAssetLocator implements IAssetLocator {

	private static final DemoAssetLocator INSTANCE = new DemoAssetLocator();

	public static @NonNull DemoAssetLocator inst() {
		return DemoAssetLocator.INSTANCE;
	}

	@Override
	public boolean supports(final @NonNull Object handle) {
		return handle instanceof String && ((String) handle).startsWith("demo:");
	}

	@Override
	public @NonNull Asset locate(final @NonNull Object handle) {
		return StreamAsset.create(JOID.class.getResourceAsStream("/assets/demo/" + ((String) handle).substring(5)));
	}

}