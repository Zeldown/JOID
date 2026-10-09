package dev.joid.lib.resource.resolver.impl;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.resolver.IResourceResolver;
import lombok.NonNull;

public class BufferedImageResourceResolver implements IResourceResolver {

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof BufferedImage;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final BufferedImage image = (BufferedImage) input;
		final String uniqueId = image.toString();
		final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, new RasterResourceDecoder(image)));
		if (callback != null) {
			callback.accept(resource);
		}
		return resource;
	}

}