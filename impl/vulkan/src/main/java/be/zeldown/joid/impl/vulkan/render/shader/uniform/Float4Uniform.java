package be.zeldown.joid.impl.vulkan.render.shader.uniform;

public final class Float4Uniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform {

	public Float4Uniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3, final float f4) {
		if (super.getMember() != null) {
			super.getMember().putFloats(f1, f2, f3, f4);
		}
	}

}