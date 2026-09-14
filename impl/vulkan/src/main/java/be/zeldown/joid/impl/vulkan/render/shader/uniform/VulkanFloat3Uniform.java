package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.Float3Uniform;

public final class VulkanFloat3Uniform extends VulkanShaderUniform implements Float3Uniform {

	public VulkanFloat3Uniform(final VulkanUniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		if (super.getMember() != null) {
			super.getMember().putFloats(f1, f2, f3);
		}
	}

}