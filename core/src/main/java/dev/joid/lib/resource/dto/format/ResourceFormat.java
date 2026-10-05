package dev.joid.lib.resource.dto.format;

import java.util.LinkedList;
import java.util.List;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.dto.format.impl.ApngResourceFormat;
import dev.joid.lib.resource.dto.format.impl.GifResourceFormat;
import dev.joid.lib.resource.dto.format.impl.SvgResourceFormat;
import dev.joid.lib.resource.dto.format.impl.VideoResourceFormat;
import dev.joid.lib.resource.dto.format.impl.WebpResourceFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFormat {

	private static final List<IResourceFormat> FORMATS = new LinkedList<>();

	static {
		ResourceFormat.register(new GifResourceFormat());
		ResourceFormat.register(new ApngResourceFormat());
		ResourceFormat.register(new VideoResourceFormat());
		ResourceFormat.register(new WebpResourceFormat());
		ResourceFormat.register(new SvgResourceFormat());
	}

	public static void register(final @NonNull IResourceFormat format) {
		ResourceFormat.FORMATS.add(0, format);
	}

	public static @NonNull IResourceDecoder decoder(final @NonNull Asset asset) {
		final byte[] header = asset.peek(512);
		for (final IResourceFormat format : ResourceFormat.FORMATS) {
			if (format.supports(header)) {
				return format.decoder(asset, header);
			}
		}
		return new RasterResourceDecoder(asset);
	}

}