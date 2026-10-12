package dev.joid.lib.resource.resolver.impl;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.resolver.IResourceResolver;
import lombok.NonNull;

public class TextureResourceResolver implements IResourceResolver {

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof ITexture;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input) {
		final ITexture texture = (ITexture) input;
		final String uniqueId = "texture_" + System.identityHashCode(texture);
		return builder.compute(uniqueId, () -> new ResourceData(uniqueId, null).texture(texture));
	}

}