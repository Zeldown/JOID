package be.zeldown.joid.lib.resource.dto.decoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import be.zeldown.joid.lib.resource.dto.decoder.impl.GifResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.ImageResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import lombok.NonNull;

public class ResourceDecoder {

	public static @NonNull IResourceDecoder image(final @NonNull InputStream inputStream) {
		return new ImageResourceDecoder(inputStream);
	}

	public static @NonNull IResourceDecoder image(final @NonNull BufferedImage image) {
		return new ImageResourceDecoder(image);
	}

	public static @NonNull IResourceDecoder gif(final @NonNull InputStream inputStream) {
		return new GifResourceDecoder(inputStream);
	}

	public static @NonNull IResourceDecoder video(final @NonNull InputStream inputStream) {
		return new VideoResourceDecoder(inputStream);
	}

	public static @NonNull IResourceDecoder video(final @NonNull InputStream inputStream, final @NonNull String path) {
		final VideoResourceDecoder decoder = new VideoResourceDecoder(inputStream);
		if (VideoResourceDecoder.isLoopDefault(path)) {
			decoder.loop(true);
		}
		return decoder;
	}

	public static @NonNull IResourceDecoder video(final @NonNull File file) {
		return new VideoResourceDecoder(file);
	}

}