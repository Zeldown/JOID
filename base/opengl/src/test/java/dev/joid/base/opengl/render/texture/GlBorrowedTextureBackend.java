package dev.joid.base.opengl.render.texture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.test.contract.IBorrowedTextureBackend;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class GlBorrowedTextureBackend implements IBorrowedTextureBackend {

	private final ISnapshotBackend     backend;
	private final Supplier<IGlBinding> bindings;
	private final List<Integer>        textures = new ArrayList<>();

	private IGlBinding binding;

	public GlBorrowedTextureBackend(final @NonNull ISnapshotBackend backend, final @NonNull Supplier<IGlBinding> bindings) {
		this.backend  = backend;
		this.bindings = bindings;
	}

	@Override
	public void destroy() {
		for (final int texture : this.textures) {
			this.binding.deleteTexture(texture);
		}
		this.textures.clear();
		this.backend.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.backend.create(width, height);
		this.binding = this.bindings.get();
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
		return GlBorrowedTexture.of(() -> (Integer) texture.get());
	}

	@Override
	public boolean isBorrowableTexture(final @NonNull Object texture) {
		return this.binding.isTexture((Integer) texture);
	}

	@Override
	public @NonNull Object createBorrowableTexture(final int width, final int height, final int color, final boolean mipmapped) {
		final int previous = this.binding.getInteger(GlConstants.TEXTURE_BINDING_2D);
		final int texture = this.binding.genTexture();
		this.textures.add(texture);
		this.binding.bindTexture(GlConstants.TEXTURE_2D, texture);
		this.binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlConstants.NEAREST);
		this.binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlConstants.NEAREST);
		this.binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_S, GlConstants.REPEAT);
		this.binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_T, GlConstants.REPEAT);

		int level = 0;
		for (int levelWidth = width, levelHeight = height; level == 0 || mipmapped && (levelWidth > 0 || levelHeight > 0); levelWidth /= 2, levelHeight /= 2, level++) {
			final int pixelWidth = Math.max(1, levelWidth);
			final int pixelHeight = Math.max(1, levelHeight);
			final int[] pixels = new int[pixelWidth * pixelHeight];
			Arrays.fill(pixels, color);
			this.binding.texImage2D(GlConstants.TEXTURE_2D, level, GlConstants.RGBA8, pixelWidth, pixelHeight, GlConstants.BGRA, GlConstants.UNSIGNED_INT_8_8_8_8_REV);
			this.binding.texSubImage2D(GlConstants.TEXTURE_2D, level, 0, 0, pixelWidth, pixelHeight, GlConstants.BGRA, GlConstants.UNSIGNED_INT_8_8_8_8_REV, pixels);
		}
		this.binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL, level - 1);
		this.binding.bindTexture(GlConstants.TEXTURE_2D, previous);
		return texture;
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readBorrowableParameters(final @NonNull Object texture) {
		final int previous = this.binding.getInteger(GlConstants.TEXTURE_BINDING_2D);
		this.binding.bindTexture(GlConstants.TEXTURE_2D, (Integer) texture);
		final Map<String, String> parameters = new TreeMap<>();
		parameters.put("MIN_FILTER", Integer.toString(this.binding.getTexParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER)));
		parameters.put("MAG_FILTER", Integer.toString(this.binding.getTexParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER)));
		parameters.put("WRAP_S", Integer.toString(this.binding.getTexParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_S)));
		parameters.put("WRAP_T", Integer.toString(this.binding.getTexParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_T)));
		this.binding.bindTexture(GlConstants.TEXTURE_2D, previous);
		return parameters;
	}

}