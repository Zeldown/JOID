package be.zeldown.joid.lib.draw.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.draw.model.utils.IDrawableModel;
import lombok.Getter;
import lombok.NonNull;

public final class DrawModel {

	@Getter private static DrawModel instance;

	private static FloatBuffer colorBuffer = ByteBuffer.allocateDirect(16 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();

	public DrawModel() {
		if (DrawModel.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawEntity.");
		}
		DrawModel.instance = this;
	}

	public void drawModel(final double x, final double y, final double size, final @NonNull IDrawableModel model) {
		this.drawModel(x, y, size, size, size, model);
	}

	public void drawModel(final double x, final double y, final double sizeX, final double sizeY, final double sizeZ, final @NonNull IDrawableModel model) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, 0D);
		GL11.glScaled(sizeX, sizeY, sizeZ);
		GL11.glRotated(180D, 0D, 1D, 0D);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHT1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.setColorBuffer(0.6F, 0.6F, 0.6F, 1.0F));

		model.render();

		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		GL11.glDisable(GL11.GL_LIGHT1);
		GL11.glDisable(GL11.GL_LIGHT0);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPopMatrix();
	}

	private FloatBuffer setColorBuffer(final float red, final float green, final float blue, final float alpha) {
		DrawModel.colorBuffer.clear();
		DrawModel.colorBuffer.put(red).put(green).put(blue).put(alpha);
		DrawModel.colorBuffer.flip();
		return DrawModel.colorBuffer;
	}

}