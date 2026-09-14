package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform;
import lombok.NonNull;

public final class LWJGL3FloatMatrixUniform extends LWJGL3ShaderUniform implements FloatMatrixUniform {

	public LWJGL3FloatMatrixUniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final int location = super.getLocation();
		final float[] copy = value.clone();

		switch (value.length) {
		case 4:
			super.getShader().queueUniform(location, () -> GL20C.glUniformMatrix2fv(location, false, copy));
			break;
		case 9:
			super.getShader().queueUniform(location, () -> GL20C.glUniformMatrix3fv(location, false, copy));
			break;
		case 16:
			super.getShader().queueUniform(location, () -> GL20C.glUniformMatrix4fv(location, false, copy));
			break;
		default:
			throw new IllegalArgumentException("Invalid matrix size");
		}
	}

}