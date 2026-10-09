package dev.joid.lib.draw.raster;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.draw.DrawUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ExternalRaster {

	@Getter
	private static ExternalRaster instance;

	private IFrameBuffer  target;
	private IRenderBridge bridge;

	public ExternalRaster() {
		if (ExternalRaster.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of ExternalRaster.");
		}
		ExternalRaster.instance = this;
	}

	public void drawRaster(final double x, final double y, final double width, final double height, final @NonNull IExternalRasterDrawable drawable) {
		if (width <= 0D || height <= 0D) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapRight(x, x + width);
		final double bottom = grid.snapBottom(y, y + height);
		final int pixelWidth = Math.min(4096, grid.toPixelWidth(Math.abs(right - left)));
		final int pixelHeight = Math.min(4096, grid.toPixelHeight(Math.abs(bottom - top)));
		final IFrameBuffer target = this.allocate(render, pixelWidth, pixelHeight);

		render.pushState();
		render.pushProjection();
		render.pushMatrix();
		try {
			render.frameBuffer(target);
			render.viewport(0, 0, pixelWidth, pixelHeight);
			render.ortho(0D, pixelWidth, pixelHeight, 0D, -1000D, 1000D);
			render.loadIdentity();
			render.clearColor(0F, 0F, 0F, 0F);
			render.clearDepth();
			render.raster(target, pixelWidth, pixelHeight, () -> drawable.draw(pixelWidth, pixelHeight));
		} finally {
			render.popMatrix();
			render.popProjection();
			render.popState();
		}

		final double u = (double) pixelWidth / target.getWidth();
		final double v = (double) pixelHeight / target.getHeight();
		DrawUtils.RESOURCE.drawTexture(left, top, right - left, bottom - top, target.getTexture(), 0D, v, u, 0D, grid.isAligned() ? TextureFilter.NEAREST : TextureFilter.LINEAR, BlendState.PREMULTIPLIED);
	}

	private IFrameBuffer allocate(final IRenderBridge render, final int width, final int height) {
		final boolean owned = this.target != null && this.bridge == render;
		if (owned && this.target.getWidth() >= width && this.target.getHeight() >= height) {
			return this.target;
		}

		final int targetWidth = ExternalRaster.capacity(owned ? this.target.getWidth() : 0, width);
		final int targetHeight = ExternalRaster.capacity(owned ? this.target.getHeight() : 0, height);
		if (owned) {
			this.target.delete();
		}

		this.bridge = render;
		this.target = render.createFrameBuffer(targetWidth, targetHeight);
		return this.target;
	}

	private static int capacity(final int current, final int required) {
		return current >= required ? current : Math.min(4096, Math.max(required, Math.max(64, current * 2)));
	}

}