package dev.joid.impl.lwjgl3.render.framebuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import dev.joid.impl.lwjgl3.render.texture.Texture;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrameBuffer implements IFrameBuffer {

	private final int     id;
	private final Texture texture;

	public static @NonNull FrameBuffer create(final int width, final int height) {
		final Texture texture = Texture.create().allocate(width, height);
		final int id = GL30C.glGenFramebuffers();
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, id);
		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, texture.getId(), 0);
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
		return new FrameBuffer(id, texture);
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
		GL30C.glDeleteFramebuffers(this.id);
		this.texture.delete();
	}

}