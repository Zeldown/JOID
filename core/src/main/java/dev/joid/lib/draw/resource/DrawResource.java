package dev.joid.lib.draw.resource;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.EdgeSmoothing;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceProperties;
import dev.joid.lib.shader.impl.RoundedShader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawResource {

	private static final DrawResource INSTANCE = new DrawResource();

	public static @NonNull DrawResource inst() {
		return DrawResource.INSTANCE;
	}

	public void drawResource(final double x, final double y, final @NonNull Resource resource) {
		this.drawResource(x, y, resource.getWidth(), resource.getHeight(), resource);
	}

	public void drawResource(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
		this.drawRegion(x, y, width, height, null, resource);
	}

	public void drawResource(final double x, final double y, final double width, final double height, final double u, final double v, final double regionWidth, final double regionHeight, final @NonNull Resource resource) {
		this.drawRegion(x, y, width, height, new double[] {u, v, regionWidth, regionHeight}, resource);
	}

	public void drawTexture(final double x, final double y, final double width, final double height, final @NonNull ITexture texture, final double u0, final double v0, final double u1, final double v1, final @NonNull TextureFilter filter, final @NonNull BlendState blend) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().blend(blend).texture(texture).textureFilter(filter).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			DrawResource.drawQuad(x, y, x + width, y + height, new double[] {u0, v0, u1, v1}, 0D);
		});
	}

	private void drawRegion(final double x, final double y, final double width, final double height, final double[] crop, final Resource resource) {
		final double[] textureCoords = resource.getProperties().getTextureCoords();
		final double[] region = textureCoords == null ? crop : crop == null ? textureCoords : new double[] {textureCoords[0] + crop[0], textureCoords[1] + crop[1], crop[2], crop[3]};
		final boolean failed = resource.isFailed();
		if (failed && !JOIDEnv.isDevMode()) {
			return;
		}

		final PixelGrid grid = BridgeHandler.RENDER.get().getPixelGrid();
		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapRight(x, x + width);
		final double bottom = grid.snapBottom(y, y + height);
		final ResourceProperties properties = resource.getProperties();
		if (!failed && properties.getTextureCoords() == null) {
			final boolean cropped = region != null && region.length == 4;
			final int pixelWidth = (int) Math.round((right - left) * grid.getScaleX() * (cropped ? resource.getSourceWidth() / region[2] : 1D));
			final int pixelHeight = (int) Math.round((bottom - top) * grid.getScaleY() * (cropped ? resource.getSourceHeight() / region[3] : 1D));
			resource.request(pixelWidth, pixelHeight);

			final ITexture texture = resource.getTexture();
			final int[] textureRegion = resource.getResourceData().getRegion();
			if (properties.getMipmap() == null && resource.isMipmappable() && properties.getInterpolation() == TextureFilter.LINEAR && texture != null && (pixelWidth < (textureRegion == null ? texture.getWidth() : textureRegion[2]) || pixelHeight < (textureRegion == null ? texture.getHeight() : textureRegion[3]))) {
				properties.mipmap(true);
			}
		}

		final double[] uv = failed || region == null || region.length != 4 ? new double[] {0D, 0D, 1D, 1D} : new double[] {region[0] / resource.getSourceWidth(), region[1] / resource.getSourceHeight(), (region[0] + region[2]) / resource.getSourceWidth(), (region[1] + region[3]) / resource.getSourceHeight()};
		BridgeHandler.RENDER.get().withState(render -> render.getModelView().with(matrix -> {
			render.getState().blend(BlendState.NORMAL);
			if (grid.isAligned()) {
				resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> DrawResource.drawQuad(left, top, right, bottom, DrawResource.toTexture(uv, resource, failed), 0D));
			} else if (render.getState().getShader() == null && RoundedShader.inst().isAvailable()) {
				resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> RoundedShader.use(0F, (float) (left + 0.5D), (float) (top + 0.5D), (float) (right - 0.5D), (float) (bottom - 0.5D), () -> {
					RoundedShader.inst().aligned(false);
					DrawResource.drawQuad(left, top, right, bottom, DrawResource.toTexture(uv, resource, failed), 1D);
				}));
			} else {
				resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> EdgeSmoothing.rect(left, top, right, bottom, DrawResource.toTexture(uv, resource, failed), 1F, 1F, 1F, 1F));
			}
		}));
	}

	private static double[] toTexture(final double[] uv, final Resource resource, final boolean failed) {
		final int[] region = resource.getResourceData().getRegion();
		final ITexture texture = resource.getTexture();
		if (failed || region == null || texture == null || texture.getWidth() <= 0 || texture.getHeight() <= 0) {
			return uv;
		}

		final double width = texture.getWidth();
		final double height = texture.getHeight();
		return new double[] {(region[0] + uv[0] * region[2]) / width, (region[1] + uv[1] * region[3]) / height, (region[0] + uv[2] * region[2]) / width, (region[1] + uv[3] * region[3]) / height};
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