package dev.joid.backend.vulkan.resource;

import dev.joid.backend.vulkan.render.texture.IVulkanImageSupplier;
import dev.joid.backend.vulkan.render.texture.VulkanBorrowedTexture;
import dev.joid.backend.vulkan.render.texture.VulkanImage;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.resolver.IResourceResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanImageResourceResolver implements IResourceResolver {

	private static final VulkanImageResourceResolver INSTANCE = new VulkanImageResourceResolver();

	public static @NonNull VulkanImageResourceResolver inst() {
		return VulkanImageResourceResolver.INSTANCE;
	}

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof VulkanImage || input instanceof IVulkanImageSupplier;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input) {
		final String uniqueId = input instanceof VulkanImage ? "vulkan_image_" + ((VulkanImage) input).getView() : "vulkan_image_supplier_" + System.identityHashCode(input);
		final VulkanBorrowedTexture texture = input instanceof VulkanImage ? VulkanBorrowedTexture.create((VulkanImage) input) : VulkanBorrowedTexture.create((IVulkanImageSupplier) input);
		return builder.compute(uniqueId, () -> new ResourceData(uniqueId, null).texture(texture));
	}

}