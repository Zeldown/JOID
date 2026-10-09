package dev.joid.backend.lwjgl2.render;

import java.nio.IntBuffer;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import dev.joid.backend.lwjgl2.snapshot.SnapshotBackend;
import dev.joid.base.opengl.render.texture.GlBorrowedTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.test.contract.IBorrowedTextureBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class BorrowedTextureBackend implements IBorrowedTextureBackend {

	private final SnapshotBackend backend = new SnapshotBackend();

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
		return GlBorrowedTexture.create(this.backend.getBridge(), () -> (Integer) texture.get());
	}

	@Override
	public boolean isHostTexture(final @NonNull Object texture) {
		return GL11.glIsTexture((Integer) texture);
	}

	@Override
	public @NonNull Object createHostTexture(final int width, final int height, final int color, final boolean mipmapped) {
		final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		int level = 0;
		for (int levelWidth = width, levelHeight = height; level == 0 || mipmapped && (levelWidth > 0 || levelHeight > 0); levelWidth /= 2, levelHeight /= 2, level++) {
			final int pixelWidth = Math.max(1, levelWidth);
			final int pixelHeight = Math.max(1, levelHeight);
			final IntBuffer pixels = BufferUtils.createIntBuffer(pixelWidth * pixelHeight);
			for (int i = 0; i < pixelWidth * pixelHeight; i++) {
				pixels.put(i, color);
			}
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGBA8, pixelWidth, pixelHeight, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
		}
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, level - 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
		return texture;
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readHostParameters(final @NonNull Object texture) {
		final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, (Integer) texture);
		final Map<String, String> parameters = new TreeMap<>();
		parameters.put("MIN_FILTER", Integer.toString(GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER)));
		parameters.put("MAG_FILTER", Integer.toString(GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER)));
		parameters.put("WRAP_S", Integer.toString(GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S)));
		parameters.put("WRAP_T", Integer.toString(GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T)));
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
		return parameters;
	}

}