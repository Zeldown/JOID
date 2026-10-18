package dev.joid.backend.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import dev.joid.backend.vulkan.render.VulkanContext;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.ShaderTranslation;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanShader extends Shader {

	private long    vertexModule;
	private long    fragmentModule;
	private long    pipelineLayout;
	private boolean programActive;
	private long    descriptorSetLayout;

	private VulkanShader(final VulkanRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		super(bridge, new VulkanShaderTranslator(), vertex, fragment, blend);
	}

	public static @NonNull VulkanShader create(final VulkanRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		return new VulkanShader(bridge, vertex, fragment, blend);
	}

	@Override
	protected void deleteProgram() {
		final VulkanRenderBridge bridge = (VulkanRenderBridge) super.getBridge();
		final VkDevice device = bridge.getContext().getDevice();
		final long vertexModule = this.vertexModule;
		final long fragmentModule = this.fragmentModule;
		final long pipelineLayout = this.pipelineLayout;
		final long descriptorSetLayout = this.descriptorSetLayout;
		bridge.getPipelineCache().invalidate(this, bridge::dispose);
		bridge.getDescriptorCache().invalidate(descriptorSetLayout, bridge::dispose);
		bridge.dispose(() -> {
			VK10.vkDestroyShaderModule(device, vertexModule, null);
			VK10.vkDestroyShaderModule(device, fragmentModule, null);
			VK10.vkDestroyPipelineLayout(device, pipelineLayout, null);
			VK10.vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);
		});

		this.vertexModule        = VK10.VK_NULL_HANDLE;
		this.programActive       = false;
		this.fragmentModule      = VK10.VK_NULL_HANDLE;
		this.pipelineLayout      = VK10.VK_NULL_HANDLE;
		this.descriptorSetLayout = VK10.VK_NULL_HANDLE;
	}

	@Override
	protected void compileProgram(final @NonNull ShaderTranslation translation) {
		final ByteBuffer vertex = VulkanShaderCompiler.compile(translation.getVertex(), ShaderStage.VERTEX);
		final ByteBuffer fragment = VulkanShaderCompiler.compile(translation.getFragment(), ShaderStage.FRAGMENT);
		if (vertex == null || fragment == null) {
			return;
		}

		final VulkanContext context = ((VulkanRenderBridge) super.getBridge()).getContext();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(super.getSamplerMap().size() + 1, stack);
			bindings.get().binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for (final UniformSampler sampler : super.getSamplerMap().values()) {
				bindings.get().binding(sampler.getUnit()).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
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
			this.programActive       = true;
		}
	}

	private static long createModule(final MemoryStack stack, final VulkanContext context, final ByteBuffer code) {
		final LongBuffer module = stack.mallocLong(1);
		VulkanContext.check(VK10.vkCreateShaderModule(context.getDevice(), VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code), null, module), "vkCreateShaderModule");
		return module.get(0);
	}

}