package dev.joid.base.opengl.resource;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.texture.GlBorrowedTexture;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.resolver.IResourceResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlTextureResourceResolver implements IResourceResolver {

	private static final GlTextureResourceResolver INSTANCE = new GlTextureResourceResolver();

	public static @NonNull GlTextureResourceResolver inst() {
		return GlTextureResourceResolver.INSTANCE;
	}

	@Override
	public boolean supports(final @NonNull Object input) {
		return (input instanceof Integer || input instanceof IntSupplier) && BridgeHandler.RENDER.getBridge(GlRenderBridge.class) != null;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final GlRenderBridge bridge = BridgeHandler.RENDER.getBridge(GlRenderBridge.class);
		final String uniqueId = input instanceof Integer ? "gl_texture_" + input : "gl_texture_supplier_" + System.identityHashCode(input);
		final GlBorrowedTexture texture = input instanceof Integer ? GlBorrowedTexture.create(bridge, (Integer) input) : GlBorrowedTexture.create(bridge, (IntSupplier) input);
		final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, null).texture(texture));
		if (callback != null) {
			callback.accept(resource);
		}
		return resource;
	}

}