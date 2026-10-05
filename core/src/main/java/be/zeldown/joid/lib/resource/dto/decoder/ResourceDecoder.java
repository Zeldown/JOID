package be.zeldown.joid.lib.resource.dto.decoder;

import java.awt.image.BufferedImage;
import java.io.File;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.decoder.impl.ImageResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceDecoder {

	private static final int HEADER = 12;

	public static @NonNull IResourceDecoder of(final @NonNull Asset asset) {
		final byte[] header = asset.peek(ResourceDecoder.HEADER);
		if (VideoResourceDecoder.isVideoHeader(header, header.length)) {
			return ResourceDecoder.video(asset, VideoResourceDecoder.isLoopByDefault(header, header.length));
		}
		return ResourceDecoder.image(asset);
	}

	public static @NonNull IResourceDecoder image(final @NonNull Asset asset) {
		return new ImageResourceDecoder(asset);
	}

	public static @NonNull IResourceDecoder image(final @NonNull BufferedImage image) {
		return new ImageResourceDecoder(image);
	}

	public static @NonNull IResourceDecoder video(final @NonNull File file) {
		return new VideoResourceDecoder(file);
	}

	public static @NonNull IResourceDecoder video(final @NonNull Asset asset) {
		return new VideoResourceDecoder(asset);
	}

	public static @NonNull IResourceDecoder video(final @NonNull Asset asset, final boolean loopByDefault) {
		final VideoResourceDecoder decoder = new VideoResourceDecoder(asset);
		if (loopByDefault) {
			decoder.loop(true);
		}
		return decoder;
	}

}