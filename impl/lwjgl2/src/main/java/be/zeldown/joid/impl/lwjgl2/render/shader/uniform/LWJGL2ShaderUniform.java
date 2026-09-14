package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import be.zeldown.joid.lib.bridge.render.shader.uniform.ShaderUniform;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class LWJGL2ShaderUniform implements ShaderUniform {

	private final int location;

}