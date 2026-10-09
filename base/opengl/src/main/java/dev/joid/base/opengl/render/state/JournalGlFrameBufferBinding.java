package dev.joid.base.opengl.render.state;

import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JournalGlFrameBufferBinding implements IGlFrameBufferBinding {

	private final IGlFrameBufferBinding binding;
	private final GlStateJournal        journal;

	public static @NonNull JournalGlFrameBufferBinding create(final @NonNull IGlFrameBufferBinding binding, final @NonNull GlStateJournal journal) {
		return new JournalGlFrameBufferBinding(binding, journal);
	}

	@Override
	public int genFramebuffer() {
		return this.binding.genFramebuffer();
	}

	@Override
	public void deleteFramebuffer(final int framebuffer) {
		this.binding.deleteFramebuffer(framebuffer);
	}

	@Override
	public void bindFramebuffer(final int target, final int framebuffer) {
		this.journal.touchFrameBuffer(target);
		this.binding.bindFramebuffer(target, framebuffer);
	}

	@Override
	public void framebufferTexture2D(final int target, final int attachment, final int textureTarget, final int texture, final int level) {
		this.binding.framebufferTexture2D(target, attachment, textureTarget, texture, level);
	}

	@Override
	public int genRenderbuffer() {
		return this.binding.genRenderbuffer();
	}

	@Override
	public void deleteRenderbuffer(final int renderbuffer) {
		this.binding.deleteRenderbuffer(renderbuffer);
	}

	@Override
	public void bindRenderbuffer(final int target, final int renderbuffer) {
		this.journal.touch(GlStateKey.RENDERBUFFER);
		this.binding.bindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void renderbufferStorage(final int target, final int internalFormat, final int width, final int height) {
		this.binding.renderbufferStorage(target, internalFormat, width, height);
	}

	@Override
	public void framebufferRenderbuffer(final int target, final int attachment, final int renderbufferTarget, final int renderbuffer) {
		this.binding.framebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
	}

	@Override
	public void blitFramebuffer(final int sourceX0, final int sourceY0, final int sourceX1, final int sourceY1, final int targetX0, final int targetY0, final int targetX1, final int targetY1, final int mask, final int filter) {
		this.binding.blitFramebuffer(sourceX0, sourceY0, sourceX1, sourceY1, targetX0, targetY0, targetX1, targetY1, mask, filter);
	}

}