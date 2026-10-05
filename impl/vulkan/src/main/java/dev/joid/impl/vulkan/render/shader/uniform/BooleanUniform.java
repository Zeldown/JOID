package dev.joid.impl.vulkan.render.shader.uniform;

public final class BooleanUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.BooleanUniform {

	public BooleanUniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final boolean value) {
		if (super.getMember() != null) {
			super.getMember().putInt(value ? 1 : 0);
		}
	}

}