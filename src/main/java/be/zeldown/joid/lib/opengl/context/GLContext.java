package be.zeldown.joid.lib.opengl.context;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import lombok.NonNull;

public final class GLContext {

	public static void matrix(final @NonNull Drawing drawing) {
		GL11.glPushMatrix();
		drawing.draw();
		GL11.glPopMatrix();
	}

	public static void scissor(final @NonNull Drawing drawing, final double x1, final double y1, final double x2, final double y2) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) x1, (int) y1, (int) x2, (int) y2);
		drawing.draw();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public static void color(final @NonNull Drawing drawing, final @NonNull Color color) {
		color.bind();
		drawing.draw();
		Color.WHITE.bind();
	}

	public static void opacity(final @NonNull Drawing drawing, final float opacity) {
		GL11.glColor4f(1F, 1F, 1F, opacity);
		drawing.draw();
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

}