package com.example.backend;

import com.example.backend.engine.EngineDevice;

import dev.joid.lib.bridge.render.texture.MipmapChain;
import dev.joid.lib.bridge.render.texture.Texture;

// #region texture
public final class EngineTexture extends Texture implements IEngineTexture {

	private final int          id;
	private final EngineDevice device;

	public EngineTexture(final EngineDevice device) {
		this.device = device;
		this.id     = device.createTexture();
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	protected void allocateStorage(final MipmapChain chain) {
		final int maxSize = this.device.getMaxTextureSize();
		if (chain.getWidth() > maxSize || chain.getHeight() > maxSize) {
			final String size = chain.getWidth() + "x" + chain.getHeight();
			throw new IllegalArgumentException(
				"A texture of " + size + " exceeds the maximum size " + maxSize + " of the engine, downscale the image"
			);
		}

		this.device.allocateLevels(this.id, chain.getWidth(), chain.getHeight(), 0, chain.getLevels());
	}

	@Override
	protected void uploadPixels(final int[] pixels, final MipmapChain chain) {
		this.device.uploadTexture(this.id, chain.getWidth(), chain.getHeight(), pixels);
		chain.forEachStep(this::copy);
	}

	@Override
	protected void generateMipmapLevels(final MipmapChain chain, final int allocatedLevels) {
		if (allocatedLevels != chain.getLevels()) {
			this.device.allocateLevels(this.id, chain.getWidth(), chain.getHeight(), 1, chain.getLevels());
		}

		chain.forEachStep(this::copy);
	}

	@Override
	protected void deleteStorage() {
		this.device.deleteTexture(this.id);
	}

	private void copy(final int level, final int fromWidth, final int fromHeight, final int width, final int height) {
		this.device.copyLevel(this.id, level, fromWidth, fromHeight, width, height);
	}

}
// #endregion