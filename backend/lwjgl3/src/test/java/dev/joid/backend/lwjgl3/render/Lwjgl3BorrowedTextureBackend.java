package dev.joid.backend.lwjgl3.render;

import java.nio.IntBuffer;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.base.opengl.render.texture.GlBorrowedTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.test.contract.IBorrowedTextureBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class Lwjgl3BorrowedTextureBackend implements IBorrowedTextureBackend {

	private final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();

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
	public boolean isBorrowableTexture(final @NonNull Object texture) {
		return GL11C.glIsTexture((Integer) texture);
	}

	@Override
	public @NonNull Object createBorrowableTexture(final int width, final int height, final int color, final boolean mipmapped) {
		final int previous = GL11C.glGetInteger(GL11C.GL_TEXTURE_BINDING_2D);
		final int texture = GL11C.glGenTextures();
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

		int level = 0;
		for (int levelWidth = width, levelHeight = height; level == 0 || mipmapped && (levelWidth > 0 || levelHeight > 0); levelWidth /= 2, levelHeight /= 2, level++) {
			final int pixelWidth = Math.max(1, levelWidth);
			final int pixelHeight = Math.max(1, levelHeight);
			final IntBuffer pixels = BufferUtils.createIntBuffer(pixelWidth * pixelHeight);
			for (int i = 0; i < pixelWidth * pixelHeight; i++) {
				pixels.put(i, color);
			}
			GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, level, GL11C.GL_RGBA8, pixelWidth, pixelHeight, 0, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
		}
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL12C.GL_TEXTURE_MAX_LEVEL, level - 1);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, previous);
		return texture;
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readBorrowableParameters(final @NonNull Object texture) {
		final int previous = GL11C.glGetInteger(GL11C.GL_TEXTURE_BINDING_2D);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, (Integer) texture);
		final Map<String, String> parameters = new TreeMap<>();
		parameters.put("MIN_FILTER", Integer.toString(GL11C.glGetTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER)));
		parameters.put("MAG_FILTER", Integer.toString(GL11C.glGetTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER)));
		parameters.put("WRAP_S", Integer.toString(GL11C.glGetTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S)));
		parameters.put("WRAP_T", Integer.toString(GL11C.glGetTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T)));
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, previous);
		return parameters;
	}

}