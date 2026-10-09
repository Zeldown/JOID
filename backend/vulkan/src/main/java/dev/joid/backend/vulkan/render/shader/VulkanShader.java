package dev.joid.backend.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import dev.joid.backend.vulkan.render.VulkanContext;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanShader extends Shader {

	private final boolean active;
	private final long    vertexModule;
	private final long    fragmentModule;
	private final long    pipelineLayout;
	private final long    descriptorSetLayout;

	private VulkanShader(final VulkanRenderBridge bridge, final VulkanShaderTranslator translator, final ShaderSource vertexSource, final ShaderSource fragmentSource, final BlendState blend, final boolean active, final long vertexModule, final long fragmentModule, final long descriptorSetLayout, final long pipelineLayout) {
		super(bridge, translator, vertexSource, fragmentSource, blend);
		this.active              = active;
		this.vertexModule        = vertexModule;
		this.fragmentModule      = fragmentModule;
		this.descriptorSetLayout = descriptorSetLayout;
		this.pipelineLayout      = pipelineLayout;
	}

	public static @NonNull VulkanShader create(final VulkanRenderBridge bridge, final ShaderSource vertexSource, final ShaderSource fragmentSource, final BlendState blend) {
		final VulkanShaderTranslator translator = VulkanShaderTranslator.create();
		final List<ShaderVariable> samplers = translator.getSamplers(vertexSource, fragmentSource);

		final ByteBuffer vertex;
		final ByteBuffer fragment;
		try {
			vertex = ShaderCompiler.compileVertex(translator.translateVertex(vertexSource, fragmentSource));
			fragment = ShaderCompiler.compileFragment(translator.translateFragment(vertexSource, fragmentSource));
		} catch (final IllegalStateException e) {
			System.err.println(e.getMessage());
			return new VulkanShader(bridge, translator, vertexSource, fragmentSource, blend, false, 0L, 0L, 0L, 0L);
		}

		final VulkanContext context = bridge.getContext();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(samplers.size() + 1, stack);
			bindings.get().binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for (int i = 0; i < samplers.size(); i++) {
				bindings.get().binding(i + 1).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			}
			bindings.flip();

			final LongBuffer descriptorSetLayout = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateDescriptorSetLayout(context.getDevice(), VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings), null, descriptorSetLayout), "vkCreateDescriptorSetLayout");

			final LongBuffer pipelineLayout = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreatePipelineLayout(context.getDevice(), VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(descriptorSetLayout), null, pipelineLayout), "vkCreatePipelineLayout");

			return new VulkanShader(bridge, translator, vertexSource, fragmentSource, blend, true, VulkanShader.createModule(stack, context, vertex), VulkanShader.createModule(stack, context, fragment), descriptorSetLayout.get(0), pipelineLayout.get(0));
		}
	}

	private static long createModule(final MemoryStack stack, final VulkanContext context, final ByteBuffer code) {
		final LongBuffer module = stack.mallocLong(1);
		VulkanContext.check(VK10.vkCreateShaderModule(context.getDevice(), VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code), null, module), "vkCreateShaderModule");
		return module.get(0);
	}

}