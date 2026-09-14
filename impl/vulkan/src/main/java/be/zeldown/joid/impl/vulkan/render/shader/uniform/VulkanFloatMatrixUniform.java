package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform;
import lombok.NonNull;

public final class VulkanFloatMatrixUniform extends VulkanShaderUniform implements FloatMatrixUniform {

	public VulkanFloatMatrixUniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		if (super.getMember() != null) {
			super.getMember().putMatrix(value);
		}
	}

}