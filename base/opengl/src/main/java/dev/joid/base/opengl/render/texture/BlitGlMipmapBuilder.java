package dev.joid.base.opengl.render.texture;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.texture.MipmapChain;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlitGlMipmapBuilder implements IGlMipmapBuilder {

	public static @NonNull BlitGlMipmapBuilder create() {
		return new BlitGlMipmapBuilder();
	}

	@Override
	public void build(final @NonNull GlRenderBridge bridge, final @NonNull GlTexture texture, final @NonNull MipmapChain chain) {
		final IGlFrameBufferBinding frameBuffer = bridge.getFrameBufferBinding();
		final int source = frameBuffer.genFramebuffer();
		final int target = frameBuffer.genFramebuffer();
		try {
			frameBuffer.bindFramebuffer(GlConstants.READ_FRAMEBUFFER, source);
			frameBuffer.bindFramebuffer(GlConstants.DRAW_FRAMEBUFFER, target);
			chain.forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
				frameBuffer.framebufferTexture2D(GlConstants.READ_FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, texture.getId(), level - 1);
				frameBuffer.framebufferTexture2D(GlConstants.DRAW_FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, texture.getId(), level);
				frameBuffer.blitFramebuffer(0, 0, sourceWidth, sourceHeight, 0, 0, targetWidth, targetHeight, GlConstants.COLOR_BUFFER_BIT, GlConstants.LINEAR);
			});
		} finally {
			frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, 0);
			frameBuffer.deleteFramebuffer(source);
			frameBuffer.deleteFramebuffer(target);
		}
	}

}