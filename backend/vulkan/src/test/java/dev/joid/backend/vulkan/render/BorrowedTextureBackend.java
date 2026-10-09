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
	public boolean isHostTexture(final @NonNull Object texture) {
		final Texture host = this.textures.get(texture);
		return host != null && !host.isDeleted() && host.getImage() == ((VulkanImage) texture).getImage();
	}

	@Override
	public @NonNull Object createHostTexture(final int width, final int height, final int color, final boolean mipmapped) {
		final int[] pixels = new int[width * height];
		Arrays.fill(pixels, color);
		final Texture host = (Texture) BridgeHandler.RENDER.get().createTexture();
		host.mipmap(mipmapped).allocate(width, height).upload(pixels, width, height);
		final VulkanImage image = VulkanImage.create(host.getImage(), host.getView(), width, height, host.getLevels());
		this.textures.put(image, host);
		return image;
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readHostParameters(final @NonNull Object texture) {
		return Collections.emptyMap();
	}

}