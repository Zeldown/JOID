package be.zeldown.joid.impl.vulkan.render.shader.uniform;

public final class IntUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.IntUniform {

	public IntUniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final int value) {
		if (super.getMember() != null) {
			super.getMember().putInt(value);
		}
	}

}