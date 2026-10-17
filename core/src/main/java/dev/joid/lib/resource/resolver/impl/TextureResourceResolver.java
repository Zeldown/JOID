package dev.joid.lib.resource.resolver.impl;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.resolver.IResourceResolver;
import lombok.NonNull;

public class TextureResourceResolver implements IResourceResolver {

	private static final AtomicLong            COUNTER = new AtomicLong();
	private static final Map<ITexture, String> IDS     = Collections.synchronizedMap(new WeakHashMap<>());

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof ITexture;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input) {
		final ITexture texture = (ITexture) input;
		final String uniqueId = TextureResourceResolver.IDS.computeIfAbsent(texture, key -> "texture_" + TextureResourceResolver.COUNTER.incrementAndGet());
		return builder.compute(uniqueId, () -> ResourceData.create(uniqueId, null).texture(texture));
	}

}