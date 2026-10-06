package dev.joid.impl.lwjgl2.render.framebuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import dev.joid.impl.lwjgl2.render.RenderBridge;
import dev.joid.impl.lwjgl2.render.texture.Texture;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrameBuffer implements IFrameBuffer {

	private final int     id;
	private final int     depth;
	private final Texture texture;

	public static @NonNull FrameBuffer create(final int width, final int height, final TextureFilter filter) {
		final Texture texture = Texture.create().allocate(width, height);
		final int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		RenderBridge.applyTextureParameters(filter, TextureWrap.CLAMP_TO_BORDER);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);

		final int previousFrameBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		final int id = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture.getId(), 0);

		final int previousRenderBuffer = GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING);
		final int depth = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depth);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depth);
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, previousRenderBuffer);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFrameBuffer);
		return new FrameBuffer(id, depth, texture);
	}

	@Override
	public int getWidth() {
		return this.texture.getWidth();
	}

	@Override
	public int getHeight() {
		return this.texture.getHeight();
	}

	@Override
	public void delete() {
		GL30.glDeleteFramebuffers(this.id);
		GL30.glDeleteRenderbuffers(this.depth);
		this.texture.delete();
	}

}