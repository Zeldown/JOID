package dev.joid.lib.render.framebuffer;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class FrameBuffer {

	private final IFrameBuffer  handle;
	private final TextureFilter filter;

	private boolean bound;
	private boolean filled;

	protected FrameBuffer(final @NonNull IFrameBuffer handle, final @NonNull TextureFilter filter) {
		this.handle = handle;
		this.filter = filter;
	}

	public static @NonNull FrameBuffer create(final int width, final int height, final @NonNull TextureFilter filter) {
		return new FrameBuffer(BridgeHandler.RENDER.get().createFrameBuffer(width, height, filter), filter);
	}

	public @NonNull FrameBuffer bind() {
		if (this.bound) {
			throw new IllegalStateException("The framebuffer is already bound, call unbind() first");
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		render.frameBuffer(this.handle);
		this.bound = true;
		return this;
	}

	public @NonNull FrameBuffer unbind() {
		if (!this.bound) {
			throw new IllegalStateException("The framebuffer is not bound, call bind() first");
		}

		this.bound = false;
		BridgeHandler.RENDER.get().popState();
		return this;
	}

	public @NonNull FrameBuffer fill(final @NonNull Runnable runnable) {
		this.bind();
		try {
			runnable.run();
		} finally {
			this.unbind();
		}

		this.filled = true;
		return this;
	}

	public @NonNull FrameBuffer draw(final double x, final double y, final double width, final double height) {
		if (!this.filled) {
			throw new RuntimeException("You have to fill the framebuffer before drawing it.");
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.blend(BlendState.NORMAL);
		render.texture(this.handle.getTexture(), this.filter, TextureWrap.CLAMP_TO_BORDER);

		final Tessellator tess = Tessellator.inst();
		tess.start(DrawMode.QUADS);
		tess.addVertexWithUV(x, y + height, 0D, 0D, 0D);
		tess.addVertexWithUV(x + width, y + height, 0D, 1D, 0D);
		tess.addVertexWithUV(x + width, y, 0D, 1D, 1D);
		tess.addVertexWithUV(x, y, 0D, 0D, 1D);
		tess.draw();

		render.blend(BlendState.DISABLED);
		render.resetTexture();
		return this;
	}

	public int getWidth() {
		return this.handle.getWidth();
	}

	public int getHeight() {
		return this.handle.getHeight();
	}

	public void delete() {
		this.handle.delete();
	}

}