package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;

public final class VulkanFloatUniform extends VulkanShaderUniform implements FloatUniform {

	public VulkanFloatUniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final float value) {
		if (super.getMember() != null) {
			super.getMember().putFloats(value);
		}
	}

}