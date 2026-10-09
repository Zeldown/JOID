package dev.joid.lib.bridge.render.texture;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class BorrowedTexture<H> implements ITexture {

	private final Supplier<H> supplier;

	protected BorrowedTexture(final @NonNull Supplier<H> supplier) {
		this.supplier = supplier;
	}

	@Override
	public final @NonNull BorrowedTexture<H> mipmap(final boolean mipmap) {
		return this;
	}

	@Override
	public final @NonNull BorrowedTexture<H> allocate(final int width, final int height) {
		throw new UnsupportedOperationException("A borrowed texture belongs to its host: JOID reads it but never allocates it, create a texture with IRenderBridge.createTexture() to draw your own pixels");
	}

	@Override
	public final @NonNull BorrowedTexture<H> upload(final @NonNull int[] pixels, final int width, final int height) {
		throw new UnsupportedOperationException("A borrowed texture belongs to its host: JOID reads it but never writes it, create a texture with IRenderBridge.createTexture() to draw your own pixels");
	}

	@Override
	public final void delete() {}

	public final H getHandle() {
		return this.supplier.get();
	}

	@Override
	public final int getWidth() {
		final H handle = this.getHandle();
		return handle != null ? this.getWidth(handle) : 0;
	}

	@Override
	public final int getHeight() {
		final H handle = this.getHandle();
		return handle != null ? this.getHeight(handle) : 0;
	}

	public final boolean isValid() {
		final H handle = this.getHandle();
		return handle != null && this.isValid(handle);
	}

	@Override
	public final boolean isMipmapped() {
		final H handle = this.getHandle();
		return handle != null && this.isMipmapped(handle);
	}

	protected abstract int getWidth(final @NonNull H handle);
	protected abstract int getHeight(final @NonNull H handle);

	protected abstract boolean isValid(final @NonNull H handle);
	protected abstract boolean isMipmapped(final @NonNull H handle);

}