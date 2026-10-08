package dev.joid.impl.lwjgl3.render.framebuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL30C;

import dev.joid.impl.lwjgl3.render.texture.Texture;
import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class FrameBuffer extends FrameBufferHandle<Texture> {

	private final int id;
	private final int depth;

	private FrameBuffer(final int id, final int depth, final Texture texture) {
		super(texture);
		this.id    = id;
		this.depth = depth;
	}

	public static @NonNull FrameBuffer create(final int width, final int height) {
		final Texture texture = Texture.create();
		texture.allocate(width, height);
		final int id = GL30C.glGenFramebuffers();
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, id);
		GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, texture.getId(), 0);

		final int depth = GL30C.glGenRenderbuffers();
		GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, depth);
		GL30C.glRenderbufferStorage(GL30C.GL_RENDERBUFFER, GL14C.GL_DEPTH_COMPONENT24, width, height);
		GL30C.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_RENDERBUFFER, depth);
		GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0);
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
		return new FrameBuffer(id, depth, texture);
	}

	@Override
	protected void onDelete() {
		GL30C.glDeleteFramebuffers(this.id);
		GL30C.glDeleteRenderbuffers(this.depth);
	}

}