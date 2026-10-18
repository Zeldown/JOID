package dev.joid.base.opengl.render.framebuffer;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.texture.GlTexture;
import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;
import lombok.Getter;

@Getter
public final class GlFrameBuffer extends FrameBufferHandle<GlTexture> {

	private final int            id;
	private final int            depth;
	private final GlRenderBridge bridge;

	public GlFrameBuffer(final GlRenderBridge bridge, final int width, final int height) {
		super(GlTexture.create(bridge));
		final IGlFrameBufferBinding frameBuffer = bridge.getFrameBufferBinding();
		super.getTexture().allocate(width, height);
		this.bridge = bridge;
		this.id     = frameBuffer.genFramebuffer();
		frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, this.id);
		frameBuffer.framebufferTexture2D(GlConstants.FRAMEBUFFER, GlConstants.COLOR_ATTACHMENT0, GlConstants.TEXTURE_2D, super.getTexture().getId(), 0);

		this.depth = frameBuffer.genRenderbuffer();
		frameBuffer.bindRenderbuffer(GlConstants.RENDERBUFFER, this.depth);
		frameBuffer.renderbufferStorage(GlConstants.RENDERBUFFER, GlConstants.DEPTH_COMPONENT24, width, height);
		frameBuffer.framebufferRenderbuffer(GlConstants.FRAMEBUFFER, GlConstants.DEPTH_ATTACHMENT, GlConstants.RENDERBUFFER, this.depth);
		frameBuffer.bindRenderbuffer(GlConstants.RENDERBUFFER, 0);
		frameBuffer.bindFramebuffer(GlConstants.FRAMEBUFFER, 0);
	}

	@Override
	protected void deleteHandle() {
		this.bridge.getFrameBufferBinding().deleteFramebuffer(this.id);
		this.bridge.getFrameBufferBinding().deleteRenderbuffer(this.depth);
	}

}