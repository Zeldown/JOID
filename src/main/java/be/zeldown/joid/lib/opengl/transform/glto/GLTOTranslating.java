package be.zeldown.joid.lib.opengl.transform.glto;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.opengl.modifier.GLVector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GLTOTranslating implements GLTO {

	private final GLVector vector;

	@Override
	public void transform() {
		GL11.glTranslated(this.vector.getX(), this.vector.getY(), this.vector.getZ());
	}

	@Override
	public void reset() {
		GL11.glTranslated(-this.vector.getX(), -this.vector.getY(), -this.vector.getZ());
	}

}