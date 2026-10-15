package dev.joid.base.opengl.render.texture;

import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlEnums;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlBorrowedTexture extends BorrowedTexture<Integer> implements IGlTexture {

	private final GlRenderBridge bridge;

	private GlBorrowedTexture(final IntSupplier texture) {
		super(texture::getAsInt);
		this.bridge = GlBorrowedTexture.findBridge();
	}

	public static @NonNull GlBorrowedTexture create(final int texture) {
		return new GlBorrowedTexture(() -> texture);
	}

	public static @NonNull GlBorrowedTexture create(final @NonNull IntSupplier texture) {
		return new GlBorrowedTexture(texture);
	}

	@Override
	public int getId() {
		return super.getHandle();
	}

	@Override
	public void sample(final @NonNull TextureSampling sampling) {
		final IGlBinding binding = this.bridge.getBinding();
		binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MIN_FILTER, GlEnums.minFilter(sampling));
		binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAG_FILTER, GlEnums.magFilter(sampling));
		binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_S, GlEnums.wrap(sampling.getWrap()));
		binding.texParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_WRAP_T, GlEnums.wrap(sampling.getWrap()));
	}

	@Override
	protected int getWidth(final @NonNull Integer texture) {
		return this.read(texture, binding -> binding.getTexLevelParameteri(GlConstants.TEXTURE_2D, 0, GlConstants.TEXTURE_WIDTH));
	}

	@Override
	protected int getHeight(final @NonNull Integer texture) {
		return this.read(texture, binding -> binding.getTexLevelParameteri(GlConstants.TEXTURE_2D, 0, GlConstants.TEXTURE_HEIGHT));
	}

	@Override
	protected boolean isValid(final @NonNull Integer texture) {
		return this.bridge.getBinding().isTexture(texture);
	}

	@Override
	protected boolean isMipmapped(final @NonNull Integer texture) {
		return this.read(texture, binding -> Math.min(binding.getTexParameteri(GlConstants.TEXTURE_2D, GlConstants.TEXTURE_MAX_LEVEL), binding.getTexLevelParameteri(GlConstants.TEXTURE_2D, 1, GlConstants.TEXTURE_WIDTH))) > 0;
	}

	private int read(final int texture, final ToIntFunction<IGlBinding> query) {
		return this.bridge.getGuard().call(() -> {
			final IGlBinding binding = this.bridge.getBinding();
			if (!binding.isTexture(texture)) {
				return 0;
			}

			final int previous = binding.getInteger(GlConstants.TEXTURE_BINDING_2D);
			binding.bindTexture(GlConstants.TEXTURE_2D, texture);
			try {
				return query.applyAsInt(binding);
			} finally {
				binding.bindTexture(GlConstants.TEXTURE_2D, previous);
			}
		});
	}

	private static @NonNull GlRenderBridge findBridge() {
		final GlRenderBridge bridge = BridgeHandler.RENDER.getBridge(GlRenderBridge.class);
		if (bridge == null) {
			throw new IllegalStateException("No OpenGL render bridge is registered, register an OpenGL backend before GlBorrowedTexture.create(...)");
		}
		return bridge;
	}

}