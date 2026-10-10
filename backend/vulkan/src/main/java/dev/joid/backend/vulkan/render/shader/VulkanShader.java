package dev.joid.backend.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

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
import dev.joid.lib.bridge.render.shader.source.ShaderTranslation;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanShader extends Shader {

	private boolean active;
	private long    vertexModule;
	private long    fragmentModule;
	private long    pipelineLayout;
	private long    descriptorSetLayout;

	private VulkanShader(final VulkanRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		super(bridge, VulkanShaderTranslator.create(), vertex, fragment, blend);
	}

	public static @NonNull VulkanShader create(final VulkanRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		return new VulkanShader(bridge, vertex, fragment, blend);
	}

	@Override
	protected void compileProgram(final @NonNull ShaderTranslation translation) {
		final ByteBuffer vertex;
		final ByteBuffer fragment;
		try {
			vertex = ShaderCompiler.compileVertex(translation.getVertex());
			fragment = ShaderCompiler.compileFragment(translation.getFragment());
		} catch (final IllegalStateException e) {
			System.err.println(e.getMessage());
			return;
		}

		final int samplers = super.getSamplerMap().size();
		final VulkanContext context = ((VulkanRenderBridge) super.getBridge()).getContext();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(samplers + 1, stack);
			bindings.get().binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for (int i = 0; i < samplers; i++) {
				bindings.get().binding(i + 1).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			}
			bindings.flip();

			final LongBuffer descriptorSetLayout = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateDescriptorSetLayout(context.getDevice(), VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings), null, descriptorSetLayout), "vkCreateDescriptorSetLayout");

			final LongBuffer pipelineLayout = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreatePipelineLayout(context.getDevice(), VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(descriptorSetLayout), null, pipelineLayout), "vkCreatePipelineLayout");

			this.descriptorSetLayout = descriptorSetLayout.get(0);
			this.pipelineLayout      = pipelineLayout.get(0);
			this.vertexModule        = VulkanShader.createModule(stack, context, vertex);
			this.fragmentModule      = VulkanShader.createModule(stack, context, fragment);
			this.active              = true;
		}
	}

	private static long createModule(final MemoryStack stack, final VulkanContext context, final ByteBuffer code) {
		final LongBuffer module = stack.mallocLong(1);
		VulkanContext.check(VK10.vkCreateShaderModule(context.getDevice(), VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code), null, module), "vkCreateShaderModule");
		return module.get(0);
	}

}