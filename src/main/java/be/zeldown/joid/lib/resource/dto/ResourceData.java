package be.zeldown.joid.lib.resource.dto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceData {

	private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(16);

	private String            uniqueId;
	private IResourceDecoder  decoder;

	private int[]   textureId;
	private int[][] data;

	private boolean generated;
	private boolean loaded;
	private boolean uploaded;

	private int width;
	private int height;

	/* [ Constructor ] */
	public ResourceData(final @NonNull String uniqueId, final IResourceDecoder decoder) {
		this.uniqueId = uniqueId;
		this.decoder  = decoder;
		if (this.decoder != null) {
			this.decoder.init(this);
		}
	}

	/* [ Query Section ] */
	public final @NonNull ResourceData uniqueId(final @NonNull String uniqueId) {
		this.uniqueId = uniqueId;
		return this;
	}

	public final @NonNull ResourceData decoder(final IResourceDecoder decoder) {
		this.decoder = decoder;
		return this;
	}

	public final @NonNull ResourceData textureId(final int textureId) {
		this.textureId = new int[] {textureId};
		return this;
	}

	public final @NonNull ResourceData textureId(final int[] textureId) {
		this.textureId = textureId;
		return this;
	}

	public final @NonNull ResourceData data(final int[][] data) {
		this.data = data;
		return this;
	}

	public final @NonNull ResourceData generated(final boolean generated) {
		this.generated = generated;
		return this;
	}

	public final @NonNull ResourceData loaded(final boolean loaded) {
		this.loaded = loaded;
		return this;
	}

	public final @NonNull ResourceData uploaded(final boolean uploaded) {
		this.uploaded = uploaded;
		return this;
	}

	public final @NonNull ResourceData width(final int width) {
		this.width = width;
		return this;
	}

	public final @NonNull ResourceData height(final int height) {
		this.height = height;
		return this;
	}

	/* [ Getter Section ] */
	public final <T extends IResourceDecoder> T getDecoder(final @NonNull Class<T> clazz) {
		if (this.decoder == null || !clazz.isAssignableFrom(this.decoder.getClass())) {
			return null;
		}
		return clazz.cast(this.decoder);
	}

	/* [ Internal Section ] */
	public final void generate(final boolean async) {
		if (this.decoder != null) {
			this.generated = true;

			this.decoder.prepare(this);
			final Runnable task = () -> {
				this.decoder.decode(this);
				this.loaded = true;
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

		if (this.decoder == null) {
			for (int i = 0; i < this.textureId.length; i++) {
				if (this.data[i] == null) {
					continue;
				}

				AllocatedTextureUtil.allocateTexture(this.textureId[i], this.width, this.height);
				AllocatedTextureUtil.uploadTexture(this.textureId[i], this.data[i], this.width, this.height);
			}
		} else {
			this.decoder.upload(this);
		}

		this.uploaded = true;
		this.data = null;
	}

	public final void clear() {
		if (this.textureId != null) {
			for (final int id : this.textureId) {
				GL11.glDeleteTextures(id);
			}
		}

		this.data = null;
		if (this.decoder != null) {
			this.decoder.clear(this);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		this.clear();
		super.finalize();
	}

}