package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform;
import lombok.NonNull;

public final class LWJGL3Float4ArrayUniform extends LWJGL3ShaderUniform implements Float4ArrayUniform {

	public LWJGL3Float4ArrayUniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] array) {
		if (array.length % 4 != 0) {
			throw new IllegalArgumentException("Invalid array size");
		}

		final int location = super.getLocation();
		final float[] copy = array.clone();
		super.getShader().queueUniform(location, () -> GL20C.glUniform4fv(location, copy));
	}

}