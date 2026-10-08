package dev.joid.lib.bridge.render.texture;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Texture implements ITexture {

	private int     width;
	private int     height;
	private int     levels;
	private boolean deleted;
	private boolean mipmapped;

	@Override
	public final @NonNull Texture mipmap(final boolean mipmap) {
		if (this.mipmapped == mipmap) {
			return this;
		}

		this.mipmapped = mipmap;
		if (mipmap && this.isAllocated()) {
			final MipmapChain chain = MipmapChain.of(this.width, this.height, true);
			this.onGenerateLevels(chain, this.levels);
			this.levels = chain.getLevels();
		}
		return this;
	}

	@Override
	public final @NonNull Texture allocate(final int width, final int height) {
		final MipmapChain chain = MipmapChain.of(width, height, this.mipmapped);
		if (this.isAllocated() && this.width == width && this.height == height && this.levels == chain.getLevels()) {
			return this;
		}

		this.onAllocate(chain);
		this.width  = width;
		this.height = height;
		this.levels = chain.getLevels();
		return this;
	}

	@Override
	public final @NonNull Texture upload(final @NonNull int[] pixels, final int width, final int height) {
		this.onUpload(pixels, MipmapChain.of(width, height, this.mipmapped));
		return this;
	}

	@Override
	public final void delete() {
		if (this.deleted) {
			return;
		}

		this.onDelete();
		this.deleted = true;
	}

	@Override
	public boolean isAllocated() {
		return this.width > 0 && !this.deleted;
	}

	protected abstract void onAllocate(final @NonNull MipmapChain chain);

	protected abstract void onUpload(final @NonNull int[] pixels, final @NonNull MipmapChain chain);

	protected abstract void onGenerateLevels(final @NonNull MipmapChain chain, final int allocatedLevels);

	protected abstract void onDelete();

}