package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.IntUniform;

public final class VulkanIntUniform extends VulkanShaderUniform implements IntUniform {

	public VulkanIntUniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final int value) {
		if (super.getMember() != null) {
			super.getMember().putInt(value);
		}
	}

}