package dev.joid.base.opengl.render.framebuffer;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.render.texture.GlTexture;
import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlFrameBuffer extends FrameBufferHandle<GlTexture> {

	private final IGlBinding binding;
	private final int        id;
	private final int        depth;

	private GlFrameBuffer(final IGlBinding binding, final int id, final int depth, final GlTexture texture) {
		super(texture);
		this.binding = binding;
		this.id      = id;
		this.depth   = depth;
	}

	public static @NonNull GlFrameBuffer create(final @NonNull IGlBinding binding, final int width, final int height) {
		final IGlFrameBufferBinding frameBuffer = binding.getFrameBufferBinding();
		final GlTexture texture = GlTexture.create(binding);
		texture.allocate(width, height);
		final int id = frameBuffer.genFramebuffer();
		frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, id);
		frameBuffer.framebufferTexture2D(GlConstants.FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, texture.getId(), 0);

		final int depth = frameBuffer.genRenderbuffer();
		frameBuffer.bindRenderbuffer(GlConstants.RENDERBUFFER, depth);
		frameBuffer.renderbufferStorage(GlConstants.RENDERBUFFER, GlConstants.DEPTH_COMPONENT24, width, height);
		frameBuffer.framebufferRenderbuffer(GlConstants.FRAMEBUFFER, GlConstants.DEPTH_ATTACHMENT, GlConstants.RENDERBUFFER, depth);
		frameBuffer.bindRenderbuffer(GlConstants.RENDERBUFFER, 0);
		frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, 0);
		return new GlFrameBuffer(binding, id, depth, texture);
	}

	@Override
	protected void onDelete() {
		this.binding.getFrameBufferBinding().deleteFramebuffer(this.id);
		this.binding.getFrameBufferBinding().deleteRenderbuffer(this.depth);
	}

}