package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float3Uniform;

public final class LWJGL3Float3Uniform extends LWJGL3ShaderUniform implements Float3Uniform {

	public LWJGL3Float3Uniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform3f(location, f1, f2, f3));
	}

}