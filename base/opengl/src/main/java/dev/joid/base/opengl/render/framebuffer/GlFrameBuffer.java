package dev.joid.base.opengl.render.framebuffer;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.render.texture.GlTexture;
import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlFrameBuffer extends FrameBufferHandle<GlTexture> {

	private final int            id;
	private final int            depth;
	private final GlRenderBridge bridge;

	private GlFrameBuffer(final GlRenderBridge bridge, final int id, final int depth, final GlTexture texture) {
		super(texture);
		this.bridge = bridge;
		this.id     = id;
		this.depth  = depth;
	}

	public static @NonNull GlFrameBuffer create(final @NonNull GlRenderBridge bridge, final int width, final int height) {
		final IGlFrameBufferBinding frameBuffer = bridge.getFrameBufferBinding();
		final GlTexture texture = GlTexture.create(bridge);
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
		return new GlFrameBuffer(bridge, id, depth, texture);
	}

	@Override
	protected void onDelete() {
		this.bridge.getFrameBufferBinding().deleteFramebuffer(this.id);
		this.bridge.getFrameBufferBinding().deleteRenderbuffer(this.depth);
	}

}