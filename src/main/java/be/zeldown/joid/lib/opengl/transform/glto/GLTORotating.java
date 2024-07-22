package be.zeldown.joid.lib.opengl.transform.glto;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.opengl.modifier.GLCoords;
import be.zeldown.joid.lib.opengl.modifier.GLRotation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GLTORotating implements GLTO {

	private final double     angle;
	private final GLRotation rotation;
	private final GLCoords   pivot;

	@Override
	public void transform() {
		final double x = this.rotation.getRawX();
		final double y = this.rotation.getRawY();
		final double z = this.rotation.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		GL11.glTranslated(pivotX, pivotY, pivotZ);
		GL11.glRotated(this.angle, x, y, z);
		GL11.glTranslated(-pivotX, -pivotY, -pivotZ);
	}

}