package dev.joid.backend.vulkan.render;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.joid.backend.vulkan.render.texture.Texture;
import dev.joid.backend.vulkan.render.texture.VulkanBorrowedTexture;
import dev.joid.backend.vulkan.render.texture.VulkanImage;
import dev.joid.backend.vulkan.snapshot.SnapshotBackend;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.test.contract.IBorrowedTextureBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class BorrowedTextureBackend implements IBorrowedTextureBackend {

	private final SnapshotBackend      backend  = new SnapshotBackend();
	private final Map<Object, Texture> textures = new IdentityHashMap<>();

	@Override
	public void destroy() {
		this.backend.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.backend.create(width, height);
	}

	@Override
	public void present() {
		this.backend.present();
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		return this.backend.capture(width, height);
	}

	@Override
	public @NonNull String getRenderer() {
		return this.backend.getRenderer();
	}

	@Override
	public @NonNull ITexture borrow(final @NonNull Supplier<Object> texture) {
		return VulkanBorrowedTexture.create(() -> (VulkanImage) texture.get());
	}

	@Override
	public boolean isBorrowableTexture(final @NonNull Object texture) {
		final Texture borrowable = this.textures.get(texture);
		return borrowable != null && !borrowable.isDeleted() && borrowable.getImage() == ((VulkanImage) texture).getImage();
	}

	@Override
	public @NonNull Object createBorrowableTexture(final int width, final int height, final int color, final boolean mipmapped) {
		final int[] pixels = new int[width * height];
		Arrays.fill(pixels, color);
		final Texture texture = (Texture) BridgeHandler.RENDER.get().createTexture();
		texture.mipmap(mipmapped).allocate(width, height).upload(pixels, width, height);
		final VulkanImage image = VulkanImage.create(texture.getImage(), texture.getView(), width, height, texture.getLevels());
		this.textures.put(image, texture);
		return image;
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readBorrowableParameters(final @NonNull Object texture) {
		return Collections.emptyMap();
	}

}