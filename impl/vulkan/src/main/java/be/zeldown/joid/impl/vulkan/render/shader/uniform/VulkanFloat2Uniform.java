package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;

public final class VulkanFloat2Uniform extends VulkanShaderUniform implements Float2Uniform {

	public VulkanFloat2Uniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		if (super.getMember() != null) {
			super.getMember().putFloats(f1, f2);
		}
	}

}