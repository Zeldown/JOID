package dev.joid.backend.vulkan.render.texture;

import java.util.function.Supplier;

import org.lwjgl.vulkan.VK10;

import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import lombok.NonNull;

public final class VulkanBorrowedTexture extends BorrowedTexture<VulkanImage> {

	private VulkanBorrowedTexture(final Supplier<VulkanImage> image) {
		super(image);
	}

	public static @NonNull VulkanBorrowedTexture create(final @NonNull VulkanImage image) {
		return new VulkanBorrowedTexture(() -> image);
	}

	public static @NonNull VulkanBorrowedTexture create(final @NonNull Supplier<VulkanImage> image) {
		return new VulkanBorrowedTexture(image);
	}

	public long getView() {
		final VulkanImage image = super.getHandle();
		return image != null ? image.getView() : VK10.VK_NULL_HANDLE;
	}

	@Override
	protected int getWidth(final @NonNull VulkanImage image) {
		return image.getWidth();
	}

	@Override
	protected int getHeight(final @NonNull VulkanImage image) {
		return image.getHeight();
	}

	@Override
	protected boolean isValid(final @NonNull VulkanImage image) {
		return image.getImage() != VK10.VK_NULL_HANDLE && image.getView() != VK10.VK_NULL_HANDLE && image.getWidth() > 0 && image.getHeight() > 0;
	}

	@Override
	protected boolean isMipmapped(final @NonNull VulkanImage image) {
		return image.getLevels() > 1;
	}

}