package dev.joid.impl.opengl.render.texture;

import dev.joid.impl.opengl.binding.GlConstants;
import dev.joid.impl.opengl.binding.IGlBinding;
import dev.joid.impl.opengl.binding.IGlFrameBufferBinding;
import dev.joid.impl.opengl.binding.IGlTextureBinding;
import dev.joid.lib.bridge.render.texture.MipmapChain;
import dev.joid.lib.bridge.render.texture.Texture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlTexture extends Texture {

	private final IGlBinding binding;
	private final int        id;

	private GlTexture(final IGlBinding binding, final int id) {
		this.binding = binding;
		this.id      = id;
	}

	public static @NonNull GlTexture create(final @NonNull IGlBinding binding) {
		return new GlTexture(binding, binding.getTextureBinding().genTexture());
	}

	@Override
	protected void onAllocate(final @NonNull MipmapChain chain) {
		final IGlTextureBinding texture = this.binding.getTextureBinding();
		texture.bindTexture(GlConstants.TEXTURE_2D, this.id);
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlConstants.NEAREST);
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlConstants.NEAREST);
		this.allocateLevels(chain, 0);
	}

	@Override
	protected void onUpload(final @NonNull int[] pixels, final @NonNull MipmapChain chain) {
		final IGlTextureBinding texture = this.binding.getTextureBinding();
		texture.bindTexture(GlConstants.TEXTURE_2D, this.id);
		texture.texSubImage2D(GlConstants.TEXTURE_2D, 0, 0, 0, chain.getWidth(), chain.getHeight(), GlConstants.BGRA, GlConstants.UNSIGNED_INT_8_8_8_8_REV, pixels);
		this.copyLevels(chain);
	}

	@Override
	protected void onGenerateLevels(final @NonNull MipmapChain chain, final int allocatedLevels) {
		this.binding.getTextureBinding().bindTexture(GlConstants.TEXTURE_2D, this.id);
		if (allocatedLevels != chain.getLevels()) {
			this.allocateLevels(chain, 1);
		}
		this.copyLevels(chain);
	}

	@Override
	protected void onDelete() {
		this.binding.getTextureBinding().deleteTexture(this.id);
	}

	private void allocateLevels(final MipmapChain chain, final int first) {
		final IGlTextureBinding texture = this.binding.getTextureBinding();
		for (int level = first; level < chain.getLevels(); level++) {
			texture.texImage2D(GlConstants.TEXTURE_2D, level, GlConstants.RGBA8, chain.getWidth(level), chain.getHeight(level), GlConstants.BGRA, GlConstants.UNSIGNED_INT_8_8_8_8_REV);
		}
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL, chain.getLevels() - 1);
	}

	private void copyLevels(final MipmapChain chain) {
		if (chain.getLevels() == 1) {
			return;
		}

		final IGlFrameBufferBinding frameBuffer = this.binding.getFrameBufferBinding();
		final int read = this.binding.getInteger(GlConstants.READ_FRAMEBUFFER_BINDING);
		final int draw = this.binding.getInteger(GlConstants.DRAW_FRAMEBUFFER_BINDING);
		final boolean scissor = this.binding.isEnabled(GlConstants.SCISSOR_TEST);
		final int source = frameBuffer.genFramebuffer();
		final int target = frameBuffer.genFramebuffer();
		this.binding.disable(GlConstants.SCISSOR_TEST);
		try {
			frameBuffer.bindFramebuffer(GlConstants.READ_FRAMEBUFFER, source);
			frameBuffer.bindFramebuffer(GlConstants.DRAW_FRAMEBUFFER, target);
			chain.forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
				frameBuffer.framebufferTexture2D(GlConstants.READ_FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, this.id, level - 1);
				frameBuffer.framebufferTexture2D(GlConstants.DRAW_FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, this.id, level);
				frameBuffer.blitFramebuffer(0, 0, sourceWidth, sourceHeight, 0, 0, targetWidth, targetHeight, GlConstants.COLOR_BUFFER_BIT, GlConstants.LINEAR);
			});
		} finally {
			frameBuffer.bindFramebuffer(GlConstants.READ_FRAMEBUFFER, read);
			frameBuffer.bindFramebuffer(GlConstants.DRAW_FRAMEBUFFER, draw);
			frameBuffer.deleteFramebuffer(source);
			frameBuffer.deleteFramebuffer(target);
			if (scissor) {
				this.binding.enable(GlConstants.SCISSOR_TEST);
			}
		}
	}

}