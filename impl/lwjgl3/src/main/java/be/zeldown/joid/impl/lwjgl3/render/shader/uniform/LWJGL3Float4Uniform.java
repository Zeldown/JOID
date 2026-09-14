package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;

public final class LWJGL3Float4Uniform extends LWJGL3ShaderUniform implements Float4Uniform {

	public LWJGL3Float4Uniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3, final float f4) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform4f(location, f1, f2, f3, f4));
	}

}