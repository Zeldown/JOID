package dev.joid.impl.vulkan.render.pipeline;

import dev.joid.impl.vulkan.render.shader.Shader;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class PipelineKey {

	private final Shader     shader;
	private final boolean    offscreen;
	private final BlendState blend;
	private final boolean    colorMask;
	private final boolean    lines;
	private final boolean    smooth;

}