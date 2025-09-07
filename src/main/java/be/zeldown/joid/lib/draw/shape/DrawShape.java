package be.zeldown.joid.lib.draw.shape;

import java.nio.ByteBuffer;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.shader.impl.CircleShader;
import be.zeldown.joid.lib.shader.impl.RoundedShader;
import be.zeldown.joid.lib.tessellator.T9R;
import be.zeldown.joid.lib.utils.bezier.Bezier;
import lombok.Getter;
import lombok.NonNull;

public final class DrawShape {

	@Getter private static DrawShape instance;

	private static int EMPTY_TEXTURE = -1;

	public DrawShape() {
		if (DrawShape.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawShape.");
		}
		DrawShape.instance = this;
	}

	/**
	 * Draws a rectangle with the specified dimensions and color.
	 *
	 * @param x      The x-coordinate of the top-left corner of the rectangle.
	 * @param y      The y-coordinate of the top-left corner of the rectangle.
	 * @param width  The width of the rectangle.
	 * @param height The height of the rectangle.
	 * @param color  The color of the rectangle. Must not be null.
	 * @throws NullPointerException if color is null.
	 */
	public void drawRect(final double x, final double y, final double width, final double height, final @NonNull Color color) {
		this.drawPolygon(color, new Vector2d(x, y + height), new Vector2d(x + width, y + height), new Vector2d(x + width, y), new Vector2d(x, y));
	}

	/**
	 * Draws a rounded rectangle with the specified dimensions, color, and radius.
	 *
	 * @param x      The x-coordinate of the top-left corner of the rectangle.
	 * @param y      The y-coordinate of the top-left corner of the rectangle.
	 * @param width  The width of the rectangle.
	 * @param height The height of the rectangle.
	 * @param color  The color of the rectangle. Must not be null.
	 * @param radius The radius of the rounded corners.
	 * @throws NullPointerException if color is null.
	 */
	public void drawRoundedRect(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius) {
		RoundedShader.use(radius, (float) (x + radius), (float) (y + radius), (float) (x + width - radius), (float) (y + height - radius), () -> {
			this.drawRect(x, y, width, height, color);
		});
	}

	/**
	 * Draws a rounded rectangle with the specified dimensions, color, and radius.
	 *
	 * @param x             The x-coordinate of the top-left corner of the
	 *                      rectangle.
	 * @param y             The y-coordinate of the top-left corner of the
	 *                      rectangle.
	 * @param width         The width of the rectangle.
	 * @param height        The height of the rectangle.
	 * @param color         The color of the rectangle. Must not be null.
	 * @param radius        The radius of the rounded corners.
	 * @param roundedLeft   Whether the left side of the rectangle should be
	 *                      rounded.
	 * @param roundedTop    Whether the top side of the rectangle should be rounded.
	 * @param roundedRight  Whether the right side of the rectangle should be
	 *                      rounded.
	 * @param roundedBottom Whether the bottom side of the rectangle should be
	 *                      rounded.
	 * @throws NullPointerException if color is null.
	 */
	public void drawRoundedRect(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final boolean roundedLeft, final boolean roundedTop, final boolean roundedRight, final boolean roundedBottom) {
		RoundedShader.use(radius, (float) (x + (roundedLeft ? radius : 0)), (float) (y + (roundedTop ? radius : 0)), (float) (x + width - (roundedRight ? radius : 0)), (float) (y + height - (roundedBottom ? radius : 0)), () -> {
			this.drawRect(x, y, width, height, color);
		});
	}

	/**
	 * Draws a filled circle with the specified position, color, and radius.
	 *
	 * @param x     The x-coordinate of the center of the circle.
	 * @param y     The y-coordinate of the center of the circle.
	 * @param color The color of the circle. Must not be null.
	 * @param radius The radius of the circle.
	 * @throws NullPointerException if color is null.
	 */
	public void drawCircle(final double x, final double y, final @NonNull Color color, final double radius) {
		final double diameter = radius * 2D;
		CircleShader.use((float) radius, (float) x, (float) y, () -> {
			this.drawRect(x - radius, y - radius, diameter, diameter, color);
		});
	}

	/**
	 * Draws a border with the specified position, size, and color.
	 *
	 * @param x      The x-coordinate of the top-left corner of the border.
	 * @param y      The y-coordinate of the top-left corner of the border.
	 * @param x2     The x-coordinate of the bottom-right corner of the border.
	 * @param y2     The y-coordinate of the bottom-right corner of the border.
	 * @param color  The color of the border. Must not be null.
	 * @throws NullPointerException if color is null.
	 */
	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color) {
		this.drawBorder(x, y, x2, y2, color, 1D);
	}

	/**
	 * Draws a border with the specified position, size, color, and stroke width.
	 *
	 * @param x      The x-coordinate of the top-left corner of the border.
	 * @param y      The y-coordinate of the top-left corner of the border.
	 * @param x2     The x-coordinate of the bottom-right corner of the border.
	 * @param y2     The y-coordinate of the bottom-right corner of the border.
	 * @param color  The color of the border. Must not be null.
	 * @param stroke The width of the border.
	 * @throws NullPointerException if color is null.
	 */
	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color, final double stroke) {
		this.drawRect(x, y - stroke, x2 - x, stroke, color);
		this.drawRect(x - stroke, y, stroke, y2 - y, color);
		this.drawRect(x, y2, x2 - x, stroke, color);
		this.drawRect(x2, y, stroke, y2 - y, color);
	}

	/**
	 * Draws a filled border with the specified position, size, and color.
	 *
	 * @param x      The x-coordinate of the top-left corner of the border.
	 * @param y      The y-coordinate of the top-left corner of the border.
	 * @param x2     The x-coordinate of the bottom-right corner of the border.
	 * @param y2     The y-coordinate of the bottom-right corner of the border.
	 * @param color  The color of the border. Must not be null.
	 * @throws NullPointerException if color is null.
	 */
	public void drawFilledBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color) {
		this.drawFilledBorder(x, y, x2, y2, color, 1D);
	}

	/**
	 * Draws a filled border with the specified position, size, color, and stroke width.
	 *
	 * @param x      The x-coordinate of the top-left corner of the border.
	 * @param y      The y-coordinate of the top-left corner of the border.
	 * @param x2     The x-coordinate of the bottom-right corner of the border.
	 * @param y2     The y-coordinate of the bottom-right corner of the border.
	 * @param color  The color of the border. Must not be null.
	 * @param stroke The width of the border.
	 * @throws NullPointerException if color is null.
	 */
	public void drawFilledBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color, final double stroke) {
		this.drawRect(x - stroke, y - stroke, x2 - x + stroke + stroke, stroke, color);
		this.drawRect(x - stroke, y, stroke, y2 - y, color);
		this.drawRect(x - stroke, y2, x2 - x + stroke + stroke, stroke, color);
		this.drawRect(x2, y, stroke, y2 - y, color);
	}

	/**
	 * Draws a polygon with the specified color and vertices.
	 *
	 * @param color  The color of the polygon. Must not be null.
	 * @param points The vertices of the polygon. Must not be null or contain null elements.
	 * @throws NullPointerException      if color or points is null.
	 * @throws IllegalArgumentException  if points array is empty.
	 * @throws IllegalArgumentException  if any element in the points array is null.
	 */
	public void drawPolygon(final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		this.drawShape(GL11.GL_POLYGON, color, points);
	}

	/**
	 * Draws a line with the specified color and vertices.
	 *
	 * @param color  The color of the line. Must not be null.
	 * @param points The vertices of the line. Must not be null or contain null elements.
	 * @throws NullPointerException      if color or points is null.
	 * @throws IllegalArgumentException  if points array is empty.
	 * @throws IllegalArgumentException  if any element in the points array is null.
	 */
	public void drawLine(final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		this.drawShape(GL11.GL_LINE_STRIP, color, points);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	/**
	 * Draws a dashed line with the specified color, dash size, and vertices.
	 *
	 * @param color    The color of the line. Must not be null.
	 * @param dashSize The size of the dashes.
	 * @param points   The vertices of the line. Must not be null or contain null
	 *                 elements.
	 * @throws NullPointerException     if color or points is null.
	 * @throws IllegalArgumentException if points array is empty.
	 * @throws IllegalArgumentException if any element in the points array is null.
	 */
	public void drawDashedLine(final @NonNull Color color, final int pattern, final float stroke, final @NonNull Vector2d @NonNull... points) {
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		GL11.glLineStipple(pattern, (short) 0xAAAA);
		this.drawLine(color, stroke, points);
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
	}

	/**
	 * Draws a line with the specified color and vertices.
	 *
	 * @param color  The color of the line. Must not be null.
	 * @param points The vertices of the line. Must not be null or contain null elements.
	 * @param stroke The width of the line.
	 * @throws NullPointerException      if color or points is null.
	 * @throws IllegalArgumentException  if points array is empty.
	 * @throws IllegalArgumentException  if any element in the points array is null.
	 */
	public void drawLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d @NonNull... points) {
		GL11.glLineWidth(stroke);
		this.drawLine(color, points);
		GL11.glLineWidth(1F);
	}

	/**
	 * Draws a curved line with the specified color, start, end, and control points.
	 *
	 * @param color   The color of the line. Must not be null.
	 * @param start   The start point of the line. Must not be null.
	 * @param end     The end point of the line. Must not be null.
	 * @param control The control point of the line. Must not be null.
	 * @throws NullPointerException if color, start, end, or control is null.
	 */
	public void drawCurvedLine(final @NonNull Color color, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		Vector2d last = start;
		final double distance = Math.sqrt(Math.pow(end.x - start.x, 2D) + Math.pow(end.y - start.y, 2D));
		for (float t = 0F; t < 1F; t += 1F / distance) {
			final Vector2d point = Bezier.quadratic(t, start, end, control);
			DrawUtils.SHAPE.drawLine(color, last, point);
			last = point;
		}
	}

	/**
	 * Draws a curved line with the specified color, start, end, and control points.
	 *
	 * @param color   The color of the line. Must not be null.
	 * @param stroke  The width of the line.
	 * @param start   The start point of the line. Must not be null.
	 * @param end     The end point of the line. Must not be null.
	 * @param control The control point of the line. Must not be null.
	 * @throws NullPointerException if color, start, end, or control is null.
	 */
	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		GL11.glLineWidth(stroke);
		this.drawCurvedLine(color, start, end, control);
		GL11.glLineWidth(1F);
	}

	/**
	 * Draws a curved line with the specified color, start, end, and control points.
	 *
	 * @param color        The color of the line. Must not be null.
	 * @param start        The start point of the line. Must not be null.
	 * @param startControl The start control point of the line. Must not be null.
	 * @param end          The end point of the line. Must not be null.
	 * @param endControl   The end control point of the line. Must not be null.
	 */
	public void drawCurvedLine(final @NonNull Color color, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		Vector2d last = start;
		final double distance = Math.sqrt(Math.pow(end.x - start.x, 2D) + Math.pow(end.y - start.y, 2D));
		for (float t = 0F; t < 1F; t += 1F / distance) {
			final Vector2d point = Bezier.cubic(t, start, startControl, end, endControl);
			DrawUtils.SHAPE.drawLine(color, last, point);
			last = point;
		}
	}

	/**
	 * Draws a curved line with the specified color, start, end, and control points.
	 *
	 * @param color        The color of the line. Must not be null.
	 * @param stroke       The width of the line.
	 * @param start        The start point of the line. Must not be null.
	 * @param startControl The start control point of the line. Must not be null.
	 * @param end          The end point of the line. Must not be null.
	 * @param endControl   The end control point of the line. Must not be null.
	 */
	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		GL11.glLineWidth(stroke);
		this.drawCurvedLine(color, start, startControl, end, endControl);
		GL11.glLineWidth(1F);
	}

	/**
	 * Draws a shape with the specified mode, color, and vertices.
	 *
	 * @param mode   The drawing mode (GL11.GL_POLYGON for filled shapes, GL11.GL_LINE_STRIP for lines).
	 * @param color  The color of the shape. Must not be null.
	 * @param points The vertices of the shape. Must not be null or contain null elements.
	 * @throws NullPointerException      if color or points is null.
	 * @throws IllegalArgumentException  if points array is empty.
	 * @throws IllegalArgumentException  if any element in the points array is null.
	 */
	public void drawShape(final int mode, final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (final Vector2d point : points) {
			minX = Math.min(minX, point.x);
			minY = Math.min(minY, point.y);
			maxX = Math.max(maxX, point.x);
			maxY = Math.max(maxY, point.y);
		}

		final T9R tessellator = T9R.inst();
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		final int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		color.bind(() -> {
			DrawShape.bindEmptyTexture();
			tessellator.start(mode);
			for (final Vector2d point : points) {
				tessellator.addVertex(point.x, point.y, 0D);
			}
			tessellator.draw();
		}, new Vector4f((float) minX, (float) minY, (float) maxX, (float) maxY));
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	private static void bindEmptyTexture() {
		if (DrawShape.EMPTY_TEXTURE == -1) {
			DrawShape.EMPTY_TEXTURE = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, DrawShape.EMPTY_TEXTURE);

			final byte[] whitePixel = {(byte) 255, (byte) 255, (byte) 255, (byte) 255};

			final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
			buffer.put(whitePixel);
			buffer.flip();

			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 1, 1, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
			return;
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, DrawShape.EMPTY_TEXTURE);
	}

}