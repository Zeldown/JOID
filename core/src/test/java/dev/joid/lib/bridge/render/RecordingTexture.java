package dev.joid.lib.bridge.render;

import dev.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class RecordingTexture implements ITexture {

	private int     width;
	private int     height;
	private int[]   pixels;
	private boolean deleted;
	private boolean mipmapped;

	@Override
	public @NonNull ITexture mipmap(final boolean mipmap) {
		this.mipmapped = mipmap;
		return this;
	}

	@Override
	public @NonNull ITexture allocate(final int width, final int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public @NonNull ITexture upload(final @NonNull int[] pixels, final int width, final int height) {
		this.pixels = pixels.clone();
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public void delete() {
		this.deleted = true;
	}

}