package be.zeldown.joid.lib.texture;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;

import lombok.NonNull;

public class VolatileTexture {

	private int glTextureId;

	private final int width;
	private final int height;

	private int[] textureData;
	private boolean uploaded;

	public VolatileTexture(final @NonNull BufferedImage bufferedImage) {
		this.glTextureId = -1;

		this.width = bufferedImage.getWidth();
		this.height = bufferedImage.getHeight();

		this.textureData = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.textureData, 0, bufferedImage.getWidth());
	}

	public void upload() {
		if (this.uploaded || this.textureData == null) {
			return;
		}

		final int allocatedTextureId = this.getGlTextureId();
		AllocatedTextureUtil.allocateTexture(allocatedTextureId, this.width, this.height);
		AllocatedTextureUtil.uploadTexture(allocatedTextureId, this.textureData, this.width, this.height);

		this.textureData = null;
		this.uploaded = true;
	}

	public void bind() {
		if (!this.uploaded) {
			this.upload();
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.glTextureId);
	}

	public int getGlTextureId() {
		if (this.glTextureId == -1) {
			this.glTextureId = GL11.glGenTextures();
		}

		return this.glTextureId;
	}

	@Override
	public void finalize() throws Throwable {
		if (this.glTextureId != -1) {
			GL11.glDeleteTextures(this.glTextureId);
		}

		super.finalize();
	}

}