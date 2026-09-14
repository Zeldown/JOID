package be.zeldown.joid.impl.vulkan.render.pipeline;

import be.zeldown.joid.impl.vulkan.render.shader.VulkanShader;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class VulkanPipelineKey {

	private final VulkanShader shader;
	private final boolean      offscreen;
	private final BlendState   blend;
	private final boolean      colorMask;
	private final boolean      lines;
	private final boolean      smooth;

}