package com.example.backend;

import java.util.function.Supplier;

import com.example.backend.engine.EngineDevice;

import dev.joid.lib.bridge.render.texture.BorrowedTexture;

// #region borrowed
public final class EngineBorrowedTexture extends BorrowedTexture<Integer> implements IEngineTexture {

	private final EngineDevice device;

	public EngineBorrowedTexture(final EngineDevice device, final Supplier<Integer> texture) {
		super(texture);
		this.device = device;
	}

	@Override
	public int getId() {
		return super.getHandle();
	}

	@Override
	protected int getWidth(final Integer texture) {
		return this.device.getTextureWidth(texture);
	}

	@Override
	protected int getHeight(final Integer texture) {
		return this.device.getTextureHeight(texture);
	}

	@Override
	protected boolean isValid(final Integer texture) {
		return this.device.isTexture(texture);
	}

	@Override
	protected boolean isMipmapped(final Integer texture) {
		return this.device.getTextureLevels(texture) > 1;
	}

}
// #endregion