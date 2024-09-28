package be.zeldown.joid.lib.resource.dto;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceData {

	private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(16);

	private String                   uniqueId;
	private BufferedImage            image;

	private int[]   textureId;
	private int[][] data;

	private boolean generated;
	private boolean loaded;
	private boolean uploaded;

	private int    width;
	private int    height;

	/* [ Constructor ] */
	public ResourceData(final @NonNull String uniqueId, final BufferedImage image) {
		this.uniqueId   = uniqueId;
		this.image      = image;
	}

	/* [ Query Section ] */
	public final @NonNull ResourceData uniqueId(final @NonNull String uniqueId) {
		this.uniqueId = uniqueId;
		return this;
	}

	public final @NonNull ResourceData image(final BufferedImage image) {
		this.image = image;
		return this;
	}

	public final @NonNull ResourceData textureId(final int textureId) {
		this.textureId = new int[] {textureId};
		return this;
	}

	/* [ Internal Section ] */
	public final void generate(final boolean async) {
		if (this.image != null) {
			this.generated = true;

			Runnable task = null;
			this.textureId = new int[] {GL11.glGenTextures()};
			AllocatedTextureUtil.allocateTexture(this.textureId[0], 1, 1);
			AllocatedTextureUtil.uploadTexture(this.textureId[0], new int[] {0}, 1, 1);

			task = () -> {
				this.width  = this.image.getWidth();
				this.height = this.image.getHeight();

				this.data = new int[1][this.width * this.height];
				this.image.getRGB(0, 0, this.width, this.height, this.data[0], 0, this.width);

				this.loaded = true;
				this.image = null;
				this.uploaded = false;
			};

			if (async) {
				ResourceData.ASYNC_EXECUTOR.execute(task);
			} else {
				task.run();
			}
		} else if (this.textureId != null && this.textureId.length > 0) {
			this.generated = true;
			final int oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId[0]);
			this.width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
			this.height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture);

			this.loaded = true;
			this.uploaded = false;
		}
	}

	public final void upload() {
		if (this.data == null) {
			return;
		}

		for (int i = 0; i < this.textureId.length; i++) {
			if (this.data[i] == null) {
				continue;
			}

			AllocatedTextureUtil.allocateTexture(this.textureId[i], this.width, this.height);
			AllocatedTextureUtil.uploadTexture(this.textureId[i], this.data[i], this.width, this.height);
		}

		this.uploaded = true;
		this.data = null;
	}

	@Override
	protected void finalize() throws Throwable {
		if (this.textureId != null) {
			for (final int id : this.textureId) {
				GL11.glDeleteTextures(id);
			}
		}

		this.image = null;
		this.data = null;

		super.finalize();
	}

}