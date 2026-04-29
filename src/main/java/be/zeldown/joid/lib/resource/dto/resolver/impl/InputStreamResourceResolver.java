package be.zeldown.joid.lib.resource.dto.resolver.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.ResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.resource.dto.resolver.IResourceResolver;
import lombok.NonNull;

public class InputStreamResourceResolver implements IResourceResolver {

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof InputStream;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final InputStream stream = (InputStream) input;
		final String uniqueId = stream.toString();
		final InputStream supportedStream = stream.markSupported() ? stream : new BufferedInputStream(stream);

		final Resource resource = builder.compute(uniqueId, () -> {
			try {
				supportedStream.mark(12);
				final byte[] header = new byte[12];
				final int read = supportedStream.read(header);
				supportedStream.reset();

				if (VideoResourceDecoder.isVideoHeader(header, read)) {
					return Resource.create(builder, new ResourceData(uniqueId, ResourceDecoder.video(supportedStream, VideoResourceDecoder.isLoopByDefault(header, read))));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
			return Resource.create(builder, new ResourceData(uniqueId, ResourceDecoder.image(supportedStream)));
		});

		if (callback != null) {
			callback.accept(resource);
		}

		return resource;
	}

}