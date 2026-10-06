package dev.joid.impl.lwjgl3.render.texture;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import dev.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Texture implements ITexture {

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
		return new Texture(GL11C.glGenTextures());
	}

	@Override
	public @NonNull Texture mipmap(final boolean mipmap) {
		if (this.mipmapped == mipmap) {
			return this;
		}

		this.mipmapped = mipmap;

		if (mipmap && this.width > 0 && this.height > 0) {
			GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
			this.generateMipmaps();
		}

		return this;
	}

	@Override
	public @NonNull Texture allocate(final int width, final int height) {
		this.width = width;
		this.height = height;

		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
		this.levels = 1;
		return this;
	}

	@Override
	public @NonNull Texture upload(final @NonNull int[] pixels, final int width, final int height) {
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		GL11C.glTexSubImage2D(GL11C.GL_TEXTURE_2D, 0, 0, 0, width, height, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);

		if (this.mipmapped) {
			this.generateMipmaps();
		}

		return this;
	}

	@Override
	public void delete() {
		if (this.deleted) {
			return;
		}

		GL11C.glDeleteTextures(this.id);
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
				GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, level, GL11C.GL_RGBA8, levelWidth, levelHeight, 0, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
			}

			GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL12C.GL_TEXTURE_MAX_LEVEL, levels - 1);
			this.levels = levels;
		}

		final int read = GL11C.glGetInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING);
		final int draw = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
		final boolean scissor = GL11C.glIsEnabled(GL11C.GL_SCISSOR_TEST);
		final int source = GL30C.glGenFramebuffers();
		final int target = GL30C.glGenFramebuffers();
		GL11C.glDisable(GL11C.GL_SCISSOR_TEST);
		try {
			GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, source);
			GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, target);
			int levelWidth = this.width;
			int levelHeight = this.height;
			for (int level = 1; level < levels; level++) {
				final int nextWidth = Math.max(1, levelWidth / 2);
				final int nextHeight = Math.max(1, levelHeight / 2);
				GL30C.glFramebufferTexture2D(GL30C.GL_READ_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, this.id, level - 1);
				GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, this.id, level);
				GL30C.glBlitFramebuffer(0, 0, levelWidth, levelHeight, 0, 0, nextWidth, nextHeight, GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_LINEAR);
				levelWidth = nextWidth;
				levelHeight = nextHeight;
			}
		} finally {
			GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, read);
			GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, draw);
			GL30C.glDeleteFramebuffers(source);
			GL30C.glDeleteFramebuffers(target);
			if (scissor) {
				GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
			}
		}
	}

}