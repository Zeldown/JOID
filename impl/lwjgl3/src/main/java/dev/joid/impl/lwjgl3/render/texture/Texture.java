package dev.joid.impl.lwjgl3.render.texture;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import dev.joid.lib.bridge.render.texture.MipmapChain;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Texture extends dev.joid.lib.bridge.render.texture.Texture {

	private final int id;

	private Texture(final int id) {
		this.id = id;
	}

	public static @NonNull Texture create() {
		return new Texture(GL11C.glGenTextures());
	}

	@Override
	protected void onAllocate(final @NonNull MipmapChain chain) {
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		Texture.allocateLevels(chain, 0);
	}

	@Override
	protected void onUpload(final @NonNull int[] pixels, final @NonNull MipmapChain chain) {
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		GL11C.glTexSubImage2D(GL11C.GL_TEXTURE_2D, 0, 0, 0, chain.getWidth(), chain.getHeight(), GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
		this.copyLevels(chain);
	}

	@Override
	protected void onGenerateLevels(final @NonNull MipmapChain chain, final int allocatedLevels) {
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
		if (allocatedLevels != chain.getLevels()) {
			Texture.allocateLevels(chain, 1);
		}
		this.copyLevels(chain);
	}

	@Override
	protected void onDelete() {
		GL11C.glDeleteTextures(this.id);
	}

	private void copyLevels(final MipmapChain chain) {
		if (chain.getLevels() == 1) {
			return;
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
			chain.forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
				GL30C.glFramebufferTexture2D(GL30C.GL_READ_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, this.id, level - 1);
				GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, this.id, level);
				GL30C.glBlitFramebuffer(0, 0, sourceWidth, sourceHeight, 0, 0, targetWidth, targetHeight, GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_LINEAR);
			});
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

	private static void allocateLevels(final MipmapChain chain, final int first) {
		for (int level = first; level < chain.getLevels(); level++) {
			GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, level, GL11C.GL_RGBA8, chain.getWidth(level), chain.getHeight(level), 0, GL12C.GL_BGRA, GL12C.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
		}
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL12C.GL_TEXTURE_MAX_LEVEL, chain.getLevels() - 1);
	}

}