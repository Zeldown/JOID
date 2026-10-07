package dev.joid.lib.draw.resource;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.dto.ResourceProperties;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DrawResource {

	@Getter
	private static DrawResource instance;

	public DrawResource() {
		if (DrawResource.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawResource.");
		}
		DrawResource.instance = this;
	}

	public void drawResource(final double x, final double y, final @NonNull Resource resource) {
		final double[] textureCoords = resource.getProperties().getTextureCoords();
		if (textureCoords != null && textureCoords.length == 4) {
			this.drawResource(x, y, textureCoords[2], textureCoords[3], resource);
			return;
		}

		this.drawResource(x, y, resource.getWidth(), resource.getHeight(), resource);
	}

	public void drawResource(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
		this.drawRegion(x, y, width, height, resource.getProperties().getTextureCoords(), resource);
	}

	public void drawResource(final double x, final double y, final double width, final double height, final double u, final double v, final double regionWidth, final double regionHeight, final @NonNull Resource resource) {
		this.drawRegion(x, y, width, height, new double[] {u, v, regionWidth, regionHeight}, resource);
	}

	private void drawRegion(final double x, final double y, final double width, final double height, final double[] region, final Resource resource) {
		final boolean failed = resource.isFailed();
		if (failed && !JOID.inst().isDevMode()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapRight(x, x + width);
		final double bottom = grid.snapBottom(y, y + height);
		final ResourceProperties properties = resource.getProperties();
		if (!failed && properties.getTextureCoords() == null) {
			final boolean cropped = region != null && region.length == 4;
			final int pixelWidth = (int) Math.round((right - left) * grid.getScaleX() * (cropped ? resource.getWidth() / region[2] : 1D));
			final int pixelHeight = (int) Math.round((bottom - top) * grid.getScaleY() * (cropped ? resource.getHeight() / region[3] : 1D));
			resource.request(pixelWidth, pixelHeight);

			final ITexture texture = resource.getTexture();
			if (properties.getMipmap() == null && resource.isMipmappable() && properties.getInterpolation() == TextureFilter.LINEAR && texture != null && (pixelWidth < texture.getWidth() || pixelHeight < texture.getHeight())) {
				properties.mipmap(true);
			}
		}

		render.pushMatrix();
		try {
			render.blend(BlendState.NORMAL);
			resource.bind(grid.isAligned() ? TextureWrap.CLAMP_TO_EDGE : TextureWrap.CLAMP_TO_BORDER, () -> {
				final Tessellator tess = Tessellator.inst();
				tess.start(DrawMode.QUADS);
				if (failed || region == null || region.length != 4) {
					tess.addVertexWithUV(left, bottom, 0D, 0D, 1D);
					tess.addVertexWithUV(right, bottom, 0D, 1D, 1D);
					tess.addVertexWithUV(right, top, 0D, 1D, 0D);
					tess.addVertexWithUV(left, top, 0D, 0D, 0D);
				} else {
					final double u = region[0] / resource.getWidth();
					final double v = region[1] / resource.getHeight();
					final double u2 = (region[0] + region[2]) / resource.getWidth();
					final double v2 = (region[1] + region[3]) / resource.getHeight();

					tess.addVertexWithUV(left, bottom, 0D, u, v2);
					tess.addVertexWithUV(right, bottom, 0D, u2, v2);
					tess.addVertexWithUV(right, top, 0D, u2, v);
					tess.addVertexWithUV(left, top, 0D, u, v);
				}
				tess.draw();

				render.blend(BlendState.DISABLED);
			});
		} finally {
			render.popMatrix();
		}
	}

}