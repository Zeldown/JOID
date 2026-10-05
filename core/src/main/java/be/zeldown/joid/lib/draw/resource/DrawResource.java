package be.zeldown.joid.lib.draw.resource;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.render.matrix.PixelGrid;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.dto.ResourceProperties;
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

	public void drawScaledResourceWidth(final double x, final double y, final double width, final @NonNull Resource resource) {
		this.drawResource(x, y, width, width * resource.getHeight() / resource.getWidth(), resource);
	}

	public void drawScaledResourceHeight(final double x, final double y, final double height, final @NonNull Resource resource) {
		this.drawResource(x, y, height * resource.getWidth() / resource.getHeight(), height, resource);
	}

	public void drawCenteredResource(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
		final double imageWidth = resource.getWidth();
		final double imageHeight = resource.getHeight();

		final double ratio = imageWidth / imageHeight;

		double scaledX = 0;
		double scaledY = 0;
		double scaledWidth = 0;
		double scaledHeight = 0;

		if (imageWidth <= imageHeight) {
			scaledWidth = width;
			scaledHeight = scaledWidth / ratio;
			scaledX = x + (width - scaledWidth) / 2;
			scaledY = y + (height - scaledHeight) / 2;
		} else {
			scaledHeight = height;
			scaledWidth = scaledHeight * ratio;
			scaledX = x + (width - scaledWidth) / 2;
			scaledY = y + (height - scaledHeight) / 2;
		}

		this.drawResource(scaledX, scaledY, scaledWidth, scaledHeight, resource);
	}

	public void drawResource(final double x, final double y, final @NonNull Resource resource) {
		this.drawResource(x, y, resource.getWidth(), resource.getHeight(), resource);
	}

	public void drawResource(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapX(x + width);
		final double bottom = grid.snapY(y + height);
		final ResourceProperties properties = resource.getProperties();
		if (properties.getTextureCoords() == null) {
			final int pixelWidth = (int) Math.round((right - left) * grid.getScaleX());
			final int pixelHeight = (int) Math.round((bottom - top) * grid.getScaleY());
			resource.request(pixelWidth, pixelHeight);

			final ITexture texture = resource.getTexture();
			if (!properties.getMipmap().isPresent() && properties.getInterpolation() == TextureFilter.LINEAR && texture != null && (pixelWidth < texture.getWidth() || pixelHeight < texture.getHeight())) {
				properties.mipmap(true);
			}
		}

		render.pushMatrix();
		render.blend(BlendState.NORMAL);
		resource.bind(grid.isAligned() ? TextureWrap.CLAMP_TO_EDGE : TextureWrap.CLAMP_TO_BORDER, () -> {
			final double[] textureCoords = resource.getProperties().getTextureCoords();

			final Tessellator tess = Tessellator.inst();
			tess.start(DrawMode.QUADS);
			if (textureCoords == null || textureCoords.length != 4) {
				tess.addVertexWithUV(left, bottom, 0D, 0D, 1D);
				tess.addVertexWithUV(right, bottom, 0D, 1D, 1D);
				tess.addVertexWithUV(right, top, 0D, 1D, 0D);
				tess.addVertexWithUV(left, top, 0D, 0D, 0D);
			} else {
				final double u = textureCoords[0];
				final double v = textureCoords[1];
				final double drawWidth = textureCoords[2];
				final double drawHeight = textureCoords[3];

				final double widthFactor = 1F / width;
				final double heightFactor = 1F / height;

				tess.addVertexWithUV(left, top + drawHeight, 0D, u * widthFactor, (v + drawHeight) * heightFactor);
				tess.addVertexWithUV(left + drawWidth, top + drawHeight, 0D, (u + drawWidth) * widthFactor, (v + drawHeight) * heightFactor);
				tess.addVertexWithUV(left + drawWidth, top, 0D, (u + drawWidth) * widthFactor, v * heightFactor);
				tess.addVertexWithUV(left, top, 0D, u * widthFactor, v * heightFactor);
			}
			tess.draw();

			render.blend(BlendState.DISABLED);
		});
		render.popMatrix();
	}

}