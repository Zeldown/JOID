package be.zeldown.joid.lib.opengl.context;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import lombok.NonNull;

public final class GLContext {

	/**
	 * Applies the matrix transformations specified by the given drawing and draws the content.
	 *
	 * @param drawing The drawing containing the matrix transformations. Must not be null.
	 * @throws NullPointerException if drawing is null.
	 */
	public static void matrix(final @NonNull Drawing drawing) {
		GL11.glPushMatrix();
		drawing.draw();
		GL11.glPopMatrix();
	}

	/**
	 * Applies scissor testing with the specified rectangular region, draws the content, and disables scissor testing.
	 *
	 * @param drawing The drawing to be scissored. Must not be null.
	 * @param x1      The x-coordinate of the scissor box's lower-left corner.
	 * @param y1      The y-coordinate of the scissor box's lower-left corner.
	 * @param x2      The x-coordinate of the scissor box's upper-right corner.
	 * @param y2      The y-coordinate of the scissor box's upper-right corner.
	 * @throws NullPointerException if drawing is null.
	 */
	public static void scissor(final @NonNull Drawing drawing, final double x1, final double y1, final double x2, final double y2) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) x1, (int) y1, (int) x2, (int) y2);
		drawing.draw();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	/**
	 * Applies the specified color and draws the content, then resets the color to white.
	 *
	 * @param drawing The drawing to be colored. Must not be null.
	 * @param color   The color to be applied. Must not be null.
	 * @throws NullPointerException if drawing or color is null.
	 */
	public static void color(final @NonNull Drawing drawing, final @NonNull Color color) {
		color.bind();
		drawing.draw();
		Color.WHITE.bind();
	}

	/**
	 * Applies the specified opacity and draws the content with the modified alpha value, then resets the alpha to 1.0.
	 *
	 * @param drawing The drawing to have opacity applied. Must not be null.
	 * @param opacity The opacity value (alpha) to be applied, ranging from 0.0 (fully transparent) to 1.0 (fully opaque).
	 * @throws NullPointerException if drawing is null.
	 */
	public static void opacity(final @NonNull Drawing drawing, final float opacity) {
		GL11.glColor4f(1F, 1F, 1F, opacity);
		drawing.draw();
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

}