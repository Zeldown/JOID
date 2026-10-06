package dev.joid.impl.lwjgl2.render.texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import dev.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Texture implements ITexture {

	private static final int       UPLOAD_BUFFER_SIZE = 2048 * 2048;
	private static final IntBuffer UPLOAD_BUFFER      = ByteBuffer.allocateDirect(Texture.UPLOAD_BUFFER_SIZE << 2).order(ByteOrder.nativeOrder()).asIntBuffer();

	private final int id;

	private int     width;
	private int     height;
	private int     levels;
	private boolean deleted;
	private boolean mipmapped;

	private Texture(final int id) {
		this.id = id;
	}

	public static @NonNull Texture create() {
		return new Texture(GL11.glGenTextures());
	}

	public static @NonNull Texture wrap(final int id) {
		final Texture texture = new Texture(id);
		final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		texture.width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		texture.height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
		return texture;
	}

	@Override
	public @NonNull Texture mipmap(final boolean mipmap) {
		if (this.mipmapped == mipmap) {
			return this;
		}

		this.mipmapped = mipmap;

		final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
		if (mipmap && this.width > 0 && this.height > 0) {
			this.generateMipmaps();
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
		return this;
	}

	@Override
	public @NonNull Texture allocate(final int width, final int height) {
		this.width = width;
		this.height = height;

		final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
		this.levels = 1;
		return this;
	}

	@Override
	public @NonNull Texture upload(final @NonNull int[] pixels, final int width, final int height) {
		final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);

		final int chunkHeight = Math.max(1, Texture.UPLOAD_BUFFER_SIZE / width);
		for (int row = 0; row < height; row += chunkHeight) {
			final int rows = Math.min(chunkHeight, height - row);
			Texture.UPLOAD_BUFFER.clear();
			Texture.UPLOAD_BUFFER.put(pixels, row * width, rows * width).flip();
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, row, width, rows, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, Texture.UPLOAD_BUFFER);
		}

		if (this.mipmapped) {
			this.generateMipmaps();
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
		return this;
	}

	@Override
	public void delete() {
		if (this.deleted) {
			return;
		}

		GL11.glDeleteTextures(this.id);
		this.deleted = true;
	}

	private void generateMipmaps() {
		final int levels = 32 - Integer.numberOfLeadingZeros(Math.max(this.width, this.height));
		if (this.levels != levels) {
			int levelWidth = this.width;
			int levelHeight = this.height;
			for (int level = 1; level < levels; level++) {
				levelWidth = Math.max(1, levelWidth / 2);
				levelHeight = Math.max(1, levelHeight / 2);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGBA, levelWidth, levelHeight, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
			}

			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, levels - 1);
			this.levels = levels;
		}

		final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		final boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
		final int source = GL30.glGenFramebuffers();
		final int target = GL30.glGenFramebuffers();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		try {
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target);
			int levelWidth = this.width;
			int levelHeight = this.height;
			for (int level = 1; level < levels; level++) {
				final int nextWidth = Math.max(1, levelWidth / 2);
				final int nextHeight = Math.max(1, levelHeight / 2);
				GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.id, level - 1);
				GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.id, level);
				GL30.glBlitFramebuffer(0, 0, levelWidth, levelHeight, 0, 0, nextWidth, nextHeight, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
				levelWidth = nextWidth;
				levelHeight = nextHeight;
			}
		} finally {
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
			GL30.glDeleteFramebuffers(source);
			GL30.glDeleteFramebuffers(target);
			if (scissor) {
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
			}
		}
	}

}