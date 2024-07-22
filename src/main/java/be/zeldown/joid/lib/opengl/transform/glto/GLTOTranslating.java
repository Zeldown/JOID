package be.zeldown.joid.lib.opengl.transform.glto;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.opengl.modifier.GLCoords;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GLTOTranslating implements GLTO {

	private final GLCoords coords;

	@Override
	public void transform() {
		GL11.glTranslated(this.coords.getX(), this.coords.getY(), this.coords.getZ());
	}

}