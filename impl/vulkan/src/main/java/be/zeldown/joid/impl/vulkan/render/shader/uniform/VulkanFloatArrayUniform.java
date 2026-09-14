package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform;
import lombok.NonNull;

public final class VulkanFloatArrayUniform extends VulkanShaderUniform implements FloatArrayUniform {

	public VulkanFloatArrayUniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		if (super.getMember() != null) {
			super.getMember().putArray(value, 1);
		}
	}

}