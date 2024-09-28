package be.zeldown.joid.lib.draw.resource;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import be.zeldown.joid.lib.opengl.context.GLContext;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.tessellator.T9R;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DrawResource {

	@Getter private static DrawResource instance;

	public DrawResource() {
		if (DrawResource.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of Resource.");
		}
		DrawResource.instance = this;
	}

	/**
	 * Draws an resource scaled proportionally based on the provided width, maintaining the aspect ratio.
	 *
	 * @param x        The x-coordinate of the top-left corner.
	 * @param y        The y-coordinate of the top-left corner.
	 * @param width    The width of the scaled resource.
	 * @param resource    The {@link ResourceLocation} of the resource.
	 * @throws NullPointerException if the provided {@link ResourceLocation} is null.
	 */
	public void drawScaledResourceWidth(final double x, final double y, final double width, final @NonNull Resource resource) {
		this.drawResource(x, y, width, width * resource.getHeight() / resource.getWidth(), resource);
	}

	/**
	 * Draws an resource scaled proportionally based on the provided height, maintaining the aspect ratio.
	 *
	 * @param x        The x-coordinate of the top-left corner.
	 * @param y        The y-coordinate of the top-left corner.
	 * @param height   The height of the scaled resource.
	 * @param resource    The {@link ResourceLocation} of the resource.
	 * @throws NullPointerException if the provided {@link ResourceLocation} is null.
	 */
	public void drawScaledResourceHeight(final double x, final double y, final double height, final @NonNull Resource resource) {
		this.drawResource(x, y, height * resource.getWidth() / resource.getHeight(), height, resource);
	}

	/**
	 * Draws an resource scaled to the specified dimensions, filling the entire area maintaining the aspect ratio.
	 *
	 * @param x                   The x-coordinate of the top-left corner.
	 * @param y                   The y-coordinate of the top-left corner.
	 * @param width               The width of the scaled resource.
	 * @param height              The height of the scaled resource.
	 * @param resource  			  The {@link ResourceLocation} of the resource.
	 * @throws NullPointerException if the provided {@link ResourceLocation} is null.
	 */
	public void drawCenteredResource(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
		final double resourceWidth = resource.getWidth();
		final double resourceHeight = resource.getHeight();

		final double ratio = resourceWidth / resourceHeight;

		double scaledX = 0;
		double scaledY = 0;
		double scaledWidth = 0;
		double scaledHeight = 0;

		if (resourceWidth <= resourceHeight) {
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

	/**
	 * Draws the resource at the specified location.
	 *
	 * @param x      The x-coordinate of the top-left corner.
	 * @param y      The y-coordinate of the top-left corner.
	 * @param resource  The {@link ResourceLocation} of the resource.
	 * @throws NullPointerException if the provided {@link ResourceLocation} is null.
	 */
	public void drawResource(final double x, final double y, final @NonNull Resource resource) {
		this.drawResource(x, y, resource.getWidth(), resource.getHeight(), resource);
	}

	/**
	 * Draws an resource on the screen with the specified coordinates, dimensions, and interpolation mode.
	 *
	 * @param x                    The x-coordinate of the top-left corner of the resource.
	 * @param y                    The y-coordinate of the top-left corner of the resource.
	 * @param width                The width of the resource.
	 * @param height               The height of the resource.
	 * @param resource                The {@link ResourceLocation} of the resource.
	 * @throws NullPointerException if the provided {@link ResourceLocation} is null.
	 */
	public void drawResource(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
		GLContext.matrix(() -> {
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_POINT_SMOOTH);
			GL14.glBlendEquation(GL14.GL_FUNC_ADD);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			resource.bind(() -> {
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

				final double[] textureCoords = resource.getProperties().getTextureCoords();

				final T9R tess = T9R.inst();
				tess.start(GL11.GL_QUADS);
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

				GL11.glDisable(GL11.GL_POINT_SMOOTH);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
			});
		});
	}

}