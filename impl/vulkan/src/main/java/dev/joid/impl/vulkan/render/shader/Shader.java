package dev.joid.impl.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import dev.joid.impl.vulkan.render.Context;
import dev.joid.impl.vulkan.render.RenderBridge;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shader extends dev.joid.lib.bridge.render.shader.Shader {

	private final RenderBridge bridge;
	private final BlendState   blend;
	private final boolean      active;
	private final long         vertexModule;
	private final long         fragmentModule;
	private final long         descriptorSetLayout;
	private final long         pipelineLayout;

	private boolean    bound;
	private BlendState previousBlend;

	private Shader(final RenderBridge bridge, final BlendState blend, final boolean active, final UniformBlock block, final List<ShaderVariable> samplers, final long vertexModule, final long fragmentModule, final long descriptorSetLayout, final long pipelineLayout) {
		super(block, samplers);
		this.bridge              = bridge;
		this.blend               = blend;
		this.active              = active;
		this.vertexModule        = vertexModule;
		this.fragmentModule      = fragmentModule;
		this.descriptorSetLayout = descriptorSetLayout;
		this.pipelineLayout      = pipelineLayout;
	}

	public static @NonNull Shader create(final RenderBridge bridge, final ShaderSource vertexSource, final ShaderSource fragmentSource, final BlendState blend) {
		final ShaderTranslator translator = ShaderTranslator.create();
		final UniformBlock block = translator.createBlock(vertexSource, fragmentSource);
		final List<ShaderVariable> samplers = translator.getSamplers(vertexSource, fragmentSource);

		final ByteBuffer vertex;
		final ByteBuffer fragment;
		try {
			vertex = ShaderCompiler.compileVertex(translator.translateVertex(vertexSource, fragmentSource));
			fragment = ShaderCompiler.compileFragment(translator.translateFragment(vertexSource, fragmentSource));
		} catch (final IllegalStateException e) {
			System.err.println(e.getMessage());
			return new Shader(bridge, blend, false, block, samplers, 0L, 0L, 0L, 0L);
		}

		final Context context = bridge.getContext();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(samplers.size() + 1, stack);
			bindings.get().binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			for (int i = 0; i < samplers.size(); i++) {
				bindings.get().binding(i + 1).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			}
			bindings.flip();

			final LongBuffer descriptorSetLayout = stack.mallocLong(1);
			Context.check(VK10.vkCreateDescriptorSetLayout(context.getDevice(), VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings), null, descriptorSetLayout), "vkCreateDescriptorSetLayout");

			final LongBuffer pipelineLayout = stack.mallocLong(1);
			Context.check(VK10.vkCreatePipelineLayout(context.getDevice(), VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(descriptorSetLayout), null, pipelineLayout), "vkCreatePipelineLayout");

			return new Shader(bridge, blend, true, block, samplers, Shader.createModule(stack, context, vertex), Shader.createModule(stack, context, fragment), descriptorSetLayout.get(0), pipelineLayout.get(0));
		}
	}

	@Override
	public void bind() {
		this.previousBlend = this.bridge.getState().getBlend();
		this.bridge.shader(this);
		this.bridge.blend(this.blend);
		this.bound = true;
	}

	@Override
	public void unbind() {
		this.bridge.shader(null);
		if (this.previousBlend != null) {
			this.bridge.blend(this.previousBlend);
			this.previousBlend = null;
		}

		this.bound = false;
	}

	private static long createModule(final MemoryStack stack, final Context context, final ByteBuffer code) {
		final LongBuffer module = stack.mallocLong(1);
		Context.check(VK10.vkCreateShaderModule(context.getDevice(), VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code), null, module), "vkCreateShaderModule");
		return module.get(0);
	}

}