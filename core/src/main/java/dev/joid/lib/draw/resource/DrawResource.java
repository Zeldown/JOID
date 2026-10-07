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
import dev.joid.lib.render.tessellator.EdgeSmoothing;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.dto.ResourceProperties;
import dev.joid.lib.shader.impl.RoundedShader;
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

		final double[] uv = failed || region == null || region.length != 4 ? new double[] {0D, 0D, 1D, 1D} : new double[] {region[0] / resource.getWidth(), region[1] / resource.getHeight(), (region[0] + region[2]) / resource.getWidth(), (region[1] + region[3]) / resource.getHeight()};
		render.pushMatrix();
		try {
			render.blend(BlendState.NORMAL);
			if (grid.isAligned()) {
				resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> DrawResource.drawQuad(left, top, right, bottom, uv, 0D));
			} else if (render.getShader() == null && RoundedShader.inst().isAvailable()) {
				resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> RoundedShader.use(0F, (float) (left + 0.5D), (float) (top + 0.5D), (float) (right - 0.5D), (float) (bottom - 0.5D), () -> {
					RoundedShader.inst().aligned(false);
					DrawResource.drawQuad(left, top, right, bottom, uv, 1D);
				}));
			} else {
				resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> EdgeSmoothing.rect(left, top, right, bottom, uv, 1F, 1F, 1F, 1F));
			}
			render.blend(BlendState.DISABLED);
		} finally {
			render.popMatrix();
		}
	}

	private static void drawQuad(final double left, final double top, final double right, final double bottom, final double[] uv, final double grow) {
		final double growU = grow == 0D ? 0D : (uv[2] - uv[0]) / (right - left) * grow;
		final double growV = grow == 0D ? 0D : (uv[3] - uv[1]) / (bottom - top) * grow;
		final Tessellator tess = Tessellator.inst();
		tess.start(DrawMode.QUADS);
		tess.addVertexWithUV(left - grow, bottom + grow, 0D, uv[0] - growU, uv[3] + growV);
		tess.addVertexWithUV(right + grow, bottom + grow, 0D, uv[2] + growU, uv[3] + growV);
		tess.addVertexWithUV(right + grow, top - grow, 0D, uv[2] + growU, uv[1] - growV);
		tess.addVertexWithUV(left - grow, top - grow, 0D, uv[0] - growU, uv[1] - growV);
		tess.draw();
	}

}