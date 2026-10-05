package dev.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import dev.joid.impl.lwjgl3.render.shader.Shader;

public final class Float4Uniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.Float4Uniform {

	public Float4Uniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3, final float f4) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform4f(location, f1, f2, f3, f4));
	}

}