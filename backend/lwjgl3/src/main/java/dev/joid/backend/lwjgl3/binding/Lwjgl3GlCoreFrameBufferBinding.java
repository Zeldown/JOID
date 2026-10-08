package dev.joid.backend.lwjgl3.binding;

import org.lwjgl.opengl.GL30C;

import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlCoreFrameBufferBinding implements IGlFrameBufferBinding {

	public static @NonNull Lwjgl3GlCoreFrameBufferBinding create() {
		return new Lwjgl3GlCoreFrameBufferBinding();
	}

	@Override
	public int genFramebuffer() {
		return GL30C.glGenFramebuffers();
	}

	@Override
	public void deleteFramebuffer(final int framebuffer) {
		GL30C.glDeleteFramebuffers(framebuffer);
	}

	@Override
	public void bindFramebuffer(final int target, final int framebuffer) {
		GL30C.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void framebufferTexture2D(final int target, final int attachment, final int textureTarget, final int texture, final int level) {
		GL30C.glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
	}

	@Override
	public int genRenderbuffer() {
		return GL30C.glGenRenderbuffers();
	}

	@Override
	public void deleteRenderbuffer(final int renderbuffer) {
		GL30C.glDeleteRenderbuffers(renderbuffer);
	}

	@Override
	public void bindRenderbuffer(final int target, final int renderbuffer) {
		GL30C.glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void renderbufferStorage(final int target, final int internalFormat, final int width, final int height) {
		GL30C.glRenderbufferStorage(target, internalFormat, width, height);
	}

	@Override
	public void framebufferRenderbuffer(final int target, final int attachment, final int renderbufferTarget, final int renderbuffer) {
		GL30C.glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
	}

	@Override
	public void blitFramebuffer(final int sourceX0, final int sourceY0, final int sourceX1, final int sourceY1, final int targetX0, final int targetY0, final int targetX1, final int targetY1, final int mask, final int filter) {
		GL30C.glBlitFramebuffer(sourceX0, sourceY0, sourceX1, sourceY1, targetX0, targetY0, targetX1, targetY1, mask, filter);
	}

}