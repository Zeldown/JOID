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
			final MipmapChain chain = MipmapChain.of(this.width, this.height, true).limit(this.getMaxLevels(this.width, this.height));
			this.generateMipmapLevels(chain, this.levels);
			this.levels = chain.getLevels();
		}
		return this;
	}

	@Override
	public final @NonNull Texture allocate(final int width, final int height) {
		if (this.deleted) {
			throw new IllegalStateException("The texture is deleted, create a new one with IRenderBridge.createTexture()");
		}

		final MipmapChain chain = MipmapChain.of(width, height, this.mipmapped).limit(this.getMaxLevels(width, height));
		if (this.isAllocated() && this.width == width && this.height == height && this.levels == chain.getLevels()) {
			return this;
		}

		this.allocateStorage(chain);
		this.width  = width;
		this.height = height;
		this.levels = chain.getLevels();
		return this;
	}

	@Override
	public final @NonNull Texture upload(final @NonNull int[] pixels) {
		if (!this.isAllocated()) {
			throw new IllegalStateException("The texture is not allocated, call allocate(width, height) before upload(pixels)");
		}

		if (pixels.length < this.width * this.height) {
			throw new IllegalArgumentException("The texture of " + this.width + "x" + this.height + " needs " + this.width * this.height + " pixels and got " + pixels.length + ", pass one pixel per texel");
		}

		this.uploadPixels(pixels, MipmapChain.of(this.width, this.height, this.mipmapped).limit(this.getMaxLevels(this.width, this.height)));
		return this;
	}

	@Override
	public final void delete() {
		if (this.deleted) {
			return;
		}

		this.deleteStorage();
		this.deleted = true;
	}

	@Override
	public boolean isAllocated() {
		return this.width > 0 && !this.deleted;
	}

	protected int getMaxLevels(final int width, final int height) {
		return Integer.MAX_VALUE;
	}

	protected abstract void allocateStorage(final @NonNull MipmapChain chain);

	protected abstract void uploadPixels(final @NonNull int[] pixels, final @NonNull MipmapChain chain);

	protected abstract void generateMipmapLevels(final @NonNull MipmapChain chain, final int allocatedLevels);

	protected abstract void deleteStorage();

}