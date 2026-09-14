package be.zeldown.joid.lib.draw.resource;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import be.zeldown.joid.lib.resource.Resource;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DrawResource {

	@Getter private static DrawResource instance;

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
		render.pushMatrix();
		render.blend(BlendState.NORMAL);
		resource.bind(TextureWrap.CLAMP_TO_BORDER, () -> {
			final double[] textureCoords = resource.getProperties().getTextureCoords();

			final Tessellator tess = Tessellator.inst();
			tess.start(DrawMode.QUADS);
			if (textureCoords == null || textureCoords.length != 4) {
				tess.addVertexWithUV(x, y + height, 0.0D, 0.0D, 1.0D);
				tess.addVertexWithUV(x + width, y + height, 0.0D, 1.0D, 1.0D);
				tess.addVertexWithUV(x + width, y, 0.0D, 1.0D, 0.0D);
				tess.addVertexWithUV(x, y, 0.0D, 0.0D, 0.0D);
			} else {
				final double u = textureCoords[0];
				final double v = textureCoords[1];
				final double drawWidth = textureCoords[2];
				final double drawHeight = textureCoords[3];

				final double widthFactor = 1.0F / width;
				final double heightFactor = 1.0F / height;

				tess.addVertexWithUV(x, y + drawHeight, 0.0D, u * widthFactor, (v + drawHeight) * heightFactor);
				tess.addVertexWithUV(x + drawWidth, y + drawHeight, 0.0D, (u + drawWidth) * widthFactor, (v + drawHeight) * heightFactor);
				tess.addVertexWithUV(x + drawWidth, y, 0.0D, (u + drawWidth) * widthFactor, v * heightFactor);
				tess.addVertexWithUV(x, y, 0.0D, u * widthFactor, v * heightFactor);
			}
			tess.draw();

			render.blend(BlendState.DISABLED);
		});
		render.popMatrix();
	}

}