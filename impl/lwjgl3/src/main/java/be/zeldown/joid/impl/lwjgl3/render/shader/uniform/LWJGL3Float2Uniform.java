package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;

public final class LWJGL3Float2Uniform extends LWJGL3ShaderUniform implements Float2Uniform {

	public LWJGL3Float2Uniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform2f(location, f1, f2));
	}

}