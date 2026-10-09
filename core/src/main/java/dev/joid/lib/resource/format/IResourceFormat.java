package dev.joid.lib.resource.format;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import lombok.NonNull;

public interface IResourceFormat {

	public boolean supports(final @NonNull byte[] header);

	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header);

}