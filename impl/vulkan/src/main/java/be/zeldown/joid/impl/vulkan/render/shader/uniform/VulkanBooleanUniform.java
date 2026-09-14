package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.BooleanUniform;

public final class VulkanBooleanUniform extends VulkanShaderUniform implements BooleanUniform {

	public VulkanBooleanUniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final boolean value) {
		if (super.getMember() != null) {
			super.getMember().putInt(value ? 1 : 0);
		}
	}

}