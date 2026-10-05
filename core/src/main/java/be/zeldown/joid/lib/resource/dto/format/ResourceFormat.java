package be.zeldown.joid.lib.resource.dto.format;

import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import be.zeldown.joid.lib.resource.dto.format.impl.ApngResourceFormat;
import be.zeldown.joid.lib.resource.dto.format.impl.GifResourceFormat;
import be.zeldown.joid.lib.resource.dto.format.impl.VideoResourceFormat;
import be.zeldown.joid.lib.resource.dto.format.impl.WebpResourceFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFormat {

	public static final int HEADER = 512;

	private static final List<IResourceFormat> FORMATS = new LinkedList<>();

	static {
		ResourceFormat.register(new GifResourceFormat());
		ResourceFormat.register(new ApngResourceFormat());
		ResourceFormat.register(new VideoResourceFormat());
		ResourceFormat.register(new WebpResourceFormat());
	}

	public static void register(final @NonNull IResourceFormat format) {
		ResourceFormat.FORMATS.add(0, format);
	}

	public static @NonNull IResourceDecoder decoder(final @NonNull Asset asset) {
		final byte[] header = asset.peek(ResourceFormat.HEADER);
		for (final IResourceFormat format : ResourceFormat.FORMATS) {
			if (format.supports(header)) {
				return format.decoder(asset, header);
			}
		}
		return new RasterResourceDecoder(asset);
	}

}