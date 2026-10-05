package dev.joid.impl.vulkan.render.shader.uniform;

public final class Float3Uniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.Float3Uniform {

	public Float3Uniform(final UniformMember member) {
		super(member);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		if (super.getMember() != null) {
			super.getMember().putFloats(f1, f2, f3);
		}
	}

}