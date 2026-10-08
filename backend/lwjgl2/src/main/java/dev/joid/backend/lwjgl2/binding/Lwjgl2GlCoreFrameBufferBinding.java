package dev.joid.backend.lwjgl2.binding;

import org.lwjgl.opengl.GL30;

import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlCoreFrameBufferBinding implements IGlFrameBufferBinding {

	public static @NonNull Lwjgl2GlCoreFrameBufferBinding create() {
		return new Lwjgl2GlCoreFrameBufferBinding();
	}

	@Override
	public int genFramebuffer() {
		return GL30.glGenFramebuffers();
	}

	@Override
	public void deleteFramebuffer(final int framebuffer) {
		GL30.glDeleteFramebuffers(framebuffer);
	}

	@Override
	public void bindFramebuffer(final int target, final int framebuffer) {
		GL30.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void framebufferTexture2D(final int target, final int attachment, final int textureTarget, final int texture, final int level) {
		GL30.glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
	}

	@Override
	public int genRenderbuffer() {
		return GL30.glGenRenderbuffers();
	}

	@Override
	public void deleteRenderbuffer(final int renderbuffer) {
		GL30.glDeleteRenderbuffers(renderbuffer);
	}

	@Override
	public void bindRenderbuffer(final int target, final int renderbuffer) {
		GL30.glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void renderbufferStorage(final int target, final int internalFormat, final int width, final int height) {
		GL30.glRenderbufferStorage(target, internalFormat, width, height);
	}

	@Override
	public void framebufferRenderbuffer(final int target, final int attachment, final int renderbufferTarget, final int renderbuffer) {
		GL30.glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
	}

	@Override
	public void blitFramebuffer(final int sourceX0, final int sourceY0, final int sourceX1, final int sourceY1, final int targetX0, final int targetY0, final int targetX1, final int targetY1, final int mask, final int filter) {
		GL30.glBlitFramebuffer(sourceX0, sourceY0, sourceX1, sourceY1, targetX0, targetY0, targetX1, targetY1, mask, filter);
	}

}