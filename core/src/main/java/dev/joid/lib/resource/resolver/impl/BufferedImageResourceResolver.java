package dev.joid.lib.resource.resolver.impl;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.resolver.IResourceResolver;
import lombok.NonNull;

public class BufferedImageResourceResolver implements IResourceResolver {

	private static final AtomicLong                 COUNTER = new AtomicLong();
	private static final Map<BufferedImage, String> IDS     = Collections.synchronizedMap(new WeakHashMap<>());

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof BufferedImage;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input) {
		final BufferedImage image = (BufferedImage) input;
		final String uniqueId = BufferedImageResourceResolver.IDS.computeIfAbsent(image, key -> "image_" + BufferedImageResourceResolver.COUNTER.incrementAndGet());
		return builder.compute(uniqueId, () -> ResourceData.create(uniqueId, RasterResourceDecoder.create(image)));
	}

}