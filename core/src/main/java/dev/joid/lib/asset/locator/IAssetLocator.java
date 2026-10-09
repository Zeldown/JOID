package dev.joid.lib.asset.locator;

import dev.joid.lib.asset.Asset;
import lombok.NonNull;

public interface IAssetLocator {

	public boolean supports(final @NonNull Object handle);

	public @NonNull Asset locate(final @NonNull Object handle);

}