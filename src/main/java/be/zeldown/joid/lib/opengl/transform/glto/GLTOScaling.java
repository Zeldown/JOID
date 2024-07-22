package be.zeldown.joid.lib.opengl.transform.glto;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.opengl.modifier.GLCoords;
import be.zeldown.joid.lib.opengl.modifier.GLScale;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GLTOScaling implements GLTO {

	private final GLScale  scale;
	private final GLCoords pivot;

	@Override
	public void transform() {
		final double x = this.scale.getRawX();
		final double y = this.scale.getRawY();
		final double z = this.scale.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		GL11.glTranslated(pivotX, pivotY, pivotZ);
		GL11.glScaled(x, y, z);
		GL11.glTranslated(-pivotX, -pivotY, -pivotZ);
	}

}