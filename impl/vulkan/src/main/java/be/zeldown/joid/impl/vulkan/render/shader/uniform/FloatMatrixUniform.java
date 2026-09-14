package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import lombok.NonNull;

public final class FloatMatrixUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform {

	public FloatMatrixUniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		if (super.getMember() != null) {
			super.getMember().putMatrix(value);
		}
	}

}