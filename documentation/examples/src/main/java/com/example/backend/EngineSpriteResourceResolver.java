package com.example.backend;

import com.example.backend.engine.EngineDevice;
import com.example.backend.engine.EngineSprite;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.resolver.IResourceResolver;

// #region resolver
public final class EngineSpriteResourceResolver implements IResourceResolver {

	private final EngineDevice device;

	public EngineSpriteResourceResolver(final EngineDevice device) {
		this.device = device;
	}

	@Override
	public boolean supports(final Object input) {
		return input instanceof EngineSprite;
	}

	@Override
	public Resource resolve(final ResourceBuilder builder, final Object input) {
		final EngineSprite sprite = (EngineSprite) input;
		final String uniqueId = "engine_sprite_" + sprite.getTexture();
		return builder.compute(uniqueId, () -> {
			final EngineBorrowedTexture texture = new EngineBorrowedTexture(this.device, sprite::getTexture);
			return ResourceData.create(uniqueId, null).texture(texture);
		});
	}

}
// #endregion