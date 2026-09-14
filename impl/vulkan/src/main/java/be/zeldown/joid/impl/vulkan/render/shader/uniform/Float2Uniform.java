package be.zeldown.joid.impl.vulkan.render.shader.uniform;

public final class Float2Uniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform {

	public Float2Uniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		if (super.getMember() != null) {
			super.getMember().putFloats(f1, f2);
		}
	}

}