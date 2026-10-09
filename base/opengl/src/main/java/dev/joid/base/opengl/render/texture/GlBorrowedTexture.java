package dev.joid.base.opengl.render.texture;

import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.render.GlEnums;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlBorrowedTexture extends BorrowedTexture<Integer> implements IGlTexture {

	private final GlRenderBridge bridge;

	private GlBorrowedTexture(final GlRenderBridge bridge, final IntSupplier texture) {
		super(texture::getAsInt);
		this.bridge = bridge;
	}

	public static @NonNull GlBorrowedTexture create(final @NonNull GlRenderBridge bridge, final int texture) {
		return new GlBorrowedTexture(bridge, () -> texture);
	}

	public static @NonNull GlBorrowedTexture create(final @NonNull GlRenderBridge bridge, final @NonNull IntSupplier texture) {
		return new GlBorrowedTexture(bridge, texture);
	}

	@Override
	public int getId() {
		final Integer texture = super.getHandle();
		return texture != null ? texture : 0;
	}

	@Override
	public void sample(final @NonNull TextureSampling sampling) {
		final IGlTextureBinding texture = this.bridge.getBinding().getTextureBinding();
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlEnums.minFilter(sampling));
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlEnums.magFilter(sampling));
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_S, GlEnums.wrap(sampling.getWrap()));
		texture.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_T, GlEnums.wrap(sampling.getWrap()));
	}

	@Override
	protected int getWidth(final @NonNull Integer texture) {
		return this.read(texture, textures -> textures.getTexLevelParameteri(GlConstants.TEXTURE_2D, 0, GlConstants.TEXTURE_WIDTH));
	}

	@Override
	protected int getHeight(final @NonNull Integer texture) {
		return this.read(texture, textures -> textures.getTexLevelParameteri(GlConstants.TEXTURE_2D, 0, GlConstants.TEXTURE_HEIGHT));
	}

	@Override
	protected boolean isValid(final @NonNull Integer texture) {
		this.bridge.getGuard().enter();
		try {
			return this.bridge.getBinding().getTextureBinding().isTexture(texture);
		} finally {
			this.bridge.getGuard().exit();
		}
	}

	@Override
	protected boolean isMipmapped(final @NonNull Integer texture) {
		return this.read(texture, textures -> Math.min(textures.getTexParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL), textures.getTexLevelParameteri(GlConstants.TEXTURE_2D, 1, GlConstants.TEXTURE_WIDTH))) > 0;
	}

	private int read(final int texture, final ToIntFunction<IGlTextureBinding> query) {
		this.bridge.getGuard().enter();
		try {
			final IGlTextureBinding textures = this.bridge.getBinding().getTextureBinding();
			if (!textures.isTexture(texture)) {
				return 0;
			}

			final int previous = this.bridge.getBinding().getInteger(GlConstants.TEXTURE_BINDING_2D);
			textures.bindTexture(GlConstants.TEXTURE_2D, texture);
			try {
				return query.applyAsInt(textures);
			} finally {
				textures.bindTexture(GlConstants.TEXTURE_2D, previous);
			}
		} finally {
			this.bridge.getGuard().exit();
		}
	}

}