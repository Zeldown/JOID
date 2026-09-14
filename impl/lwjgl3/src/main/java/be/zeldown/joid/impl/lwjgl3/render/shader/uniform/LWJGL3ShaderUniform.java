package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.ShaderUniform;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class LWJGL3ShaderUniform implements ShaderUniform {

	private final LWJGL3Shader shader;
	private final int          location;

}