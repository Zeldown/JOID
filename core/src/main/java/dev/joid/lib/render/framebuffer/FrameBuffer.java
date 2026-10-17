package dev.joid.lib.render.framebuffer;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FrameBuffer {

	@NonNull private final IFrameBuffer  handle;
	@NonNull private final TextureFilter filter;

	private boolean filled;

	public static @NonNull FrameBuffer create(final int width, final int height, final @NonNull TextureFilter filter) {
		return new FrameBuffer(BridgeHandler.RENDER.get().createFrameBuffer(width, height), filter);
	}

	public @NonNull FrameBuffer fill(final @NonNull Runnable runnable) {
		return this.fill(0D, 0D, this.getWidth(), this.getHeight(), runnable);
	}

	public @NonNull FrameBuffer fill(final double x, final double y, final double width, final double height, final @NonNull Runnable runnable) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().frameBuffer(this.handle).viewport(0, 0, this.getWidth(), this.getHeight());
			render.getProjection().with(projection -> render.getModelView().with(modelView -> {
				projection.ortho(x, x + width, y + height, y, -1000D, 1000D);
				modelView.identity();
				runnable.run();
			}));
		});

		this.filled = true;
		return this;
	}

	public @NonNull FrameBuffer draw(final double x, final double y, final double width, final double height) {
		if (!this.filled) {
			throw new IllegalStateException("The framebuffer of " + this.getWidth() + "x" + this.getHeight() + " is empty, call fill(...) before draw(...)");
		}

		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().blend(BlendState.PREMULTIPLIED);
			render.getState().texture(this.handle.getTexture()).textureFilter(this.filter).textureWrap(TextureWrap.CLAMP_TO_BORDER);

			final Tessellator tess = Tessellator.inst();
			tess.start(DrawMode.QUADS);
			tess.addVertexWithUV(x, y + height, 0D, 0D, 0D);
			tess.addVertexWithUV(x + width, y + height, 0D, 1D, 0D);
			tess.addVertexWithUV(x + width, y, 0D, 1D, 1D);
			tess.addVertexWithUV(x, y, 0D, 0D, 1D);
			tess.draw();
		});
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