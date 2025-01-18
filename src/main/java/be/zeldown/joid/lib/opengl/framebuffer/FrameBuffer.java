package be.zeldown.joid.lib.opengl.framebuffer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import be.zeldown.joid.lib.tessellator.T9R;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class FrameBuffer {

	private int framebuffer = -1;
	private int texture = -1;
	private int depth = -1;

	private int width;
	private int height;

	public @NonNull FrameBuffer prepare(final int width, final int height, final int interpolation) {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		if (this.framebuffer != -1) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

			GL30.glDeleteRenderbuffers(this.depth);
			GL11.glDeleteTextures(this.texture);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glDeleteFramebuffers(this.framebuffer);

			this.framebuffer = -1;
			this.texture = -1;
			this.depth = -1;
		}

		this.framebuffer = GL30.glGenFramebuffers();
		this.texture = GL11.glGenTextures();
		this.depth = GL30.glGenRenderbuffers();

		this.width = width;
		this.height = height;

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.texture, 0);

		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.depth);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, this.width, this.height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.depth);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		return this;
	}

	public @NonNull FrameBuffer clear() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		return this;
	}

	public @NonNull FrameBuffer bind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		return this;
	}

	public @NonNull FrameBuffer unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		return this;
	}

	public @NonNull FrameBuffer fill(final @NonNull Runnable runnable) {
		this.bind();
		runnable.run();
		this.unbind();
		return this;
	}

	public @NonNull FrameBuffer draw(final double width, final double height) {
		if (this.width == 0 || this.height == 0) {
			throw new RuntimeException("You have to prepare the framebuffer before drawing it.");
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		final T9R tess = T9R.inst();
		tess.start(GL11.GL_QUADS);
		tess.vertexUV(0, 0 + height, 0D, 0D, 0D);
		tess.vertexUV(0 + width, 0 + height, 0D, 1D, 0D);
		tess.vertexUV(0 + width, 0, 0D, 1D, 1D);
		tess.vertexUV(0, 0, 0D, 0D, 1D);
		tess.draw();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_BLEND);
		return this;
	}

	public void delete() {
		GL30.glDeleteFramebuffers(this.framebuffer);
		GL11.glDeleteTextures(this.texture);
	}

	@Override
	protected void finalize() throws Throwable {
		this.delete();
		super.finalize();
	}

}