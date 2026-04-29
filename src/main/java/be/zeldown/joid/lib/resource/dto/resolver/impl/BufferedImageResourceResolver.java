package be.zeldown.joid.lib.resource.dto.resolver.impl;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.ResourceDecoder;
import be.zeldown.joid.lib.resource.dto.resolver.IResourceResolver;
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
		final Resource resource = builder.compute(uniqueId, () -> Resource.create(builder, new ResourceData(uniqueId, ResourceDecoder.image(image))));
		if (callback != null) {
			callback.accept(resource);
		}
		return resource;
	}

}