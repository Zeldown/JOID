package dev.joid.base.opengl.render.texture;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.render.GlEnums;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.texture.MipmapChain;
import dev.joid.lib.bridge.render.texture.Texture;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlTexture extends Texture {

	private final int            id;
	private final GlRenderBridge bridge;

	private TextureSampling sampling;

	private GlTexture(final GlRenderBridge bridge, final int id) {
		this.bridge = bridge;
		this.id     = id;
	}

	public static @NonNull GlTexture create(final @NonNull GlRenderBridge bridge) {
		return new GlTexture(bridge, bridge.getBinding().getTextureBinding().genTexture());
	}

	public void sample(final @NonNull TextureSampling sampling) {
		if (sampling == this.sampling) {
			return;
		}

		final IGlTextureBinding texture = this.bridge.getBinding().getTextureBinding();
		if (this.sampling == null || GlEnums.minFilter(sampling) != GlEnums.minFilter(this.sampling)) {
			texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlEnums.minFilter(sampling));
		}
		if (this.sampling == null || sampling.getFilter() != this.sampling.getFilter()) {
			texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlEnums.magFilter(sampling));
		}
		if (this.sampling == null || sampling.getWrap() != this.sampling.getWrap()) {
			texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_S, GlEnums.wrap(sampling.getWrap()));
			texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_T, GlEnums.wrap(sampling.getWrap()));
		}
		this.sampling = sampling;
	}

	public void forgetSampling() {
		this.sampling = null;
	}

	@Override
	protected void onAllocate(final @NonNull MipmapChain chain) {
		final int maxSize = this.bridge.getCapabilities().getMaxTextureSize();
		if (chain.getWidth() > maxSize || chain.getHeight() > maxSize) {
			throw new IllegalArgumentException("A texture of " + chain.getWidth() + "x" + chain.getHeight() + " exceeds the maximum size " + maxSize + " of " + this.bridge.getCapabilities().getName());
		}

		this.bridge.getGuard().enter();
		try {
			final IGlTextureBinding texture = this.bridge.getBinding().getTextureBinding();
			texture.bindTexture(GlConstants.TEXTURE_2D, this.id);
			texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlConstants.NEAREST);
			texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlConstants.NEAREST);
			this.sampling = null;
			this.allocateLevels(chain, 0);
		} finally {
			this.bridge.getGuard().exit();
		}
	}

	@Override
	protected void onUpload(final @NonNull int[] pixels, final @NonNull MipmapChain chain) {
		this.bridge.getGuard().enter();
		try {
			final IGlTextureBinding texture = this.bridge.getBinding().getTextureBinding();
			texture.bindTexture(GlConstants.TEXTURE_2D, this.id);
			texture.texSubImage2D(GlConstants.TEXTURE_2D, 0, 0, 0, chain.getWidth(), chain.getHeight(), GlConstants.BGRA, GlConstants.UNSIGNED_INT_8_8_8_8_REV, pixels);
			this.buildLevels(chain);
		} finally {
			this.bridge.getGuard().exit();
		}
	}

	@Override
	protected void onGenerateLevels(final @NonNull MipmapChain chain, final int allocatedLevels) {
		this.bridge.getGuard().enter();
		try {
			this.bridge.getBinding().getTextureBinding().bindTexture(GlConstants.TEXTURE_2D, this.id);
			if (allocatedLevels != chain.getLevels()) {
				this.allocateLevels(chain, 1);
			}
			this.buildLevels(chain);
		} finally {
			this.bridge.getGuard().exit();
		}
	}

	@Override
	protected void onDelete() {
		this.bridge.getBinding().getTextureBinding().deleteTexture(this.id);
	}

	private void allocateLevels(final MipmapChain chain, final int first) {
		final IGlTextureBinding texture = this.bridge.getBinding().getTextureBinding();
		for (int level = first; level < chain.getLevels(); level++) {
			texture.texImage2D(GlConstants.TEXTURE_2D, level, GlConstants.RGBA8, chain.getWidth(level), chain.getHeight(level), GlConstants.BGRA, GlConstants.UNSIGNED_INT_8_8_8_8_REV);
		}
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL, chain.getLevels() - 1);
	}

	private void buildLevels(final MipmapChain chain) {
		if (chain.getLevels() > 1) {
			this.bridge.getMipmapBuilder().build(this.bridge, this, chain);
		}
	}

}