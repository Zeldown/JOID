package be.zeldown.joid.lib.opengl.framebuffer;

import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import be.zeldown.joid.lib.tessellator.T9R;
import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class FrameBuffer {

	private final int framebuffer;
	private final int texture;

	private int width;
	private int height;

	private boolean filled;

	public FrameBuffer() {
		this.framebuffer = GL30.glGenFramebuffers();
		this.texture = GL11.glGenTextures();
	}

	public @NonNull FrameBuffer prepare(final int width, final int height, final int interpolation) {
		this.width = width;
		this.height = height;

		AllocatedTextureUtil.allocateTexture(this.texture, this.width, this.height);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interpolation);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, interpolation);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.texture, 0);
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

		this.filled = true;
		return this;
	}

	public @NonNull FrameBuffer draw(final Vector4f canvas) {
		return this.draw(canvas.x, canvas.y, canvas.z, canvas.w);
	}

	public @NonNull FrameBuffer draw(final Vector4d canvas) {
		return this.draw(canvas.x, canvas.y, canvas.z, canvas.w);
	}

	public @NonNull FrameBuffer draw(final double x, final double y, final double width, final double height) {
		if (this.width == 0 || this.height == 0) {
			throw new RuntimeException("You have to prepare the framebuffer before drawing it.");
		}

		if (!this.filled) {
			throw new RuntimeException("You have to fill the framebuffer before drawing it.");
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

		final T9R tess = T9R.inst();
		tess.start(GL11.GL_QUADS);
		tess.addVertexWithUV(x, y + height, 0D, 0D, 0D);
		tess.addVertexWithUV(x + width, y + height, 0D, 1D, 0D);
		tess.addVertexWithUV(x + width, y, 0D, 1D, 1D);
		tess.addVertexWithUV(x, y, 0D, 0D, 1D);
		tess.draw();

		GL11.glDisable(GL11.GL_POINT_SMOOTH);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
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