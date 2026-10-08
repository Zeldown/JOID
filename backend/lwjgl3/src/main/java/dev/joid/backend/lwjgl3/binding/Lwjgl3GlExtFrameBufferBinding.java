package dev.joid.backend.lwjgl3.binding;

import org.lwjgl.opengl.EXTFramebufferBlit;
import org.lwjgl.opengl.EXTFramebufferObject;

import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlExtFrameBufferBinding implements IGlFrameBufferBinding {

	public static @NonNull Lwjgl3GlExtFrameBufferBinding create() {
		return new Lwjgl3GlExtFrameBufferBinding();
	}

	@Override
	public int genFramebuffer() {
		return EXTFramebufferObject.glGenFramebuffersEXT();
	}

	@Override
	public void deleteFramebuffer(final int framebuffer) {
		EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffer);
	}

	@Override
	public void bindFramebuffer(final int target, final int framebuffer) {
		EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer);
	}

	@Override
	public void framebufferTexture2D(final int target, final int attachment, final int textureTarget, final int texture, final int level) {
		EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textureTarget, texture, level);
	}

	@Override
	public int genRenderbuffer() {
		return EXTFramebufferObject.glGenRenderbuffersEXT();
	}

	@Override
	public void deleteRenderbuffer(final int renderbuffer) {
		EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
	}

	@Override
	public void bindRenderbuffer(final int target, final int renderbuffer) {
		EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
	}

	@Override
	public void renderbufferStorage(final int target, final int internalFormat, final int width, final int height) {
		EXTFramebufferObject.glRenderbufferStorageEXT(target, internalFormat, width, height);
	}

	@Override
	public void framebufferRenderbuffer(final int target, final int attachment, final int renderbufferTarget, final int renderbuffer) {
		EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderbufferTarget, renderbuffer);
	}

	@Override
	public void blitFramebuffer(final int sourceX0, final int sourceY0, final int sourceX1, final int sourceY1, final int targetX0, final int targetY0, final int targetX1, final int targetY1, final int mask, final int filter) {
		EXTFramebufferBlit.glBlitFramebufferEXT(sourceX0, sourceY0, sourceX1, sourceY1, targetX0, targetY0, targetX1, targetY1, mask, filter);
	}

}