package dev.joid.impl.lwjgl3.render.shader.uniform;

import dev.joid.impl.lwjgl3.render.shader.Shader;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.ShaderUniform {

	private final Shader shader;
	private final int    location;

}