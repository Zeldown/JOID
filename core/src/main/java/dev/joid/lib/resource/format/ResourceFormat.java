package dev.joid.lib.resource.format;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.format.impl.ApngResourceFormat;
import dev.joid.lib.resource.format.impl.GifResourceFormat;
import dev.joid.lib.resource.format.impl.HeifResourceFormat;
import dev.joid.lib.resource.format.impl.SvgResourceFormat;
import dev.joid.lib.resource.format.impl.VideoResourceFormat;
import dev.joid.lib.resource.format.impl.WebpResourceFormat;
import dev.joid.lib.utils.registry.Registry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFormat {

	private static final Registry<IResourceFormat> FORMATS = Registry.create();

	static {
		ResourceFormat.register(new GifResourceFormat());
		ResourceFormat.register(new ApngResourceFormat());
		ResourceFormat.register(new VideoResourceFormat());
		ResourceFormat.register(new HeifResourceFormat());
		ResourceFormat.register(new WebpResourceFormat());
		ResourceFormat.register(new SvgResourceFormat());
	}

	public static void register(final @NonNull IResourceFormat format) {
		ResourceFormat.FORMATS.register(format);
	}

	public static void unregister(final @NonNull IResourceFormat format) {
		ResourceFormat.FORMATS.unregister(format);
	}

	public static @NonNull IResourceDecoder decoder(final @NonNull Asset asset) {
		final byte[] header = asset.peek(512);
		final IResourceFormat format = ResourceFormat.FORMATS.find(candidate -> candidate.supports(header));
		return format == null ? new RasterResourceDecoder(asset) : format.decoder(asset, header);
	}

}