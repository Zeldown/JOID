package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform;
import lombok.NonNull;

public final class VulkanFloat4ArrayUniform extends VulkanShaderUniform implements Float4ArrayUniform {

	public VulkanFloat4ArrayUniform(final VulkanUniformMember member) {
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