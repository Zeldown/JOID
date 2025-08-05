package be.zeldown.joid.lib.shader.uniform.impl;

import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.uniform.SamplerUniform;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class DirectSamplerUniform extends DirectShaderUniform implements SamplerUniform {

	private final int textureUnit;
	private final GLShader shader;

	private int textureId;

	public DirectSamplerUniform(final int location, final int textureUnit, final @NonNull GLShader shader) {
		super(location);
		this.textureUnit = textureUnit;
		this.shader = shader;
	}

	@Override
	public void setValue(final int textureId) {
		this.textureId = textureId;

		if (this.shader.isBound()) {
			this.shader.bindTexture(this.textureUnit, this.textureId);
		}
	}

}