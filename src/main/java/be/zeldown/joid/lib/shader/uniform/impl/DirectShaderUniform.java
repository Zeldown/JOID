package be.zeldown.joid.lib.shader.uniform.impl;

import be.zeldown.joid.lib.shader.uniform.ShaderUniform;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class DirectShaderUniform implements ShaderUniform {

	private final int location;

}