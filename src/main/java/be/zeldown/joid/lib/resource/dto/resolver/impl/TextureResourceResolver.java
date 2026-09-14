package be.zeldown.joid.lib.resource.dto.resolver.impl;

import java.util.function.Consumer;

import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.resolver.IResourceResolver;
import lombok.NonNull;

public class TextureResourceResolver implements IResourceResolver {

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof ITexture;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final ITexture texture = (ITexture) input;
		final String uniqueId = "texture_" + System.identityHashCode(texture);
		final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, null).texture(texture));
		if (callback != null) {
			callback.accept(resource);
		}
		return resource;
	}

}