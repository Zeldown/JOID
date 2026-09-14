package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import lombok.NonNull;

public final class Float4ArrayUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform {

	public Float4ArrayUniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final @NonNull float[] array) {
		if (array.length % 4 != 0) {
			throw new IllegalArgumentException("Invalid array size");
		}

		if (super.getMember() != null) {
			super.getMember().putArray(array, 4);
		}
	}

}