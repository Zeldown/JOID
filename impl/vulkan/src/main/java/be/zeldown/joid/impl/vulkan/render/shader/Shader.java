package be.zeldown.joid.impl.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import be.zeldown.joid.impl.vulkan.render.Context;
import be.zeldown.joid.impl.vulkan.render.RenderBridge;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.BooleanUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.Float3Uniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.Float4ArrayUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.FloatArrayUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.FloatMatrixUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.FloatUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.IntUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.SamplerUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.UniformBlock;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.UniformMember;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Shader implements IShader {

	private final RenderBridge                bridge;
	private final BlendState                  blend;
	private final boolean                     active;
	private final long                        vertexModule;
	private final long                        fragmentModule;
	private final long                        descriptorSetLayout;
	private final long                        pipelineLayout;
	private final List<UniformBlock>          blocks;
	private final Map<String, Integer>        samplerBindings;
	private final Map<String, UniformMember>  memberMap;
	private final Map<String, SamplerUniform> samplerMap;

	private boolean    bound;
	private BlendState previousBlend;

	public static @NonNull Shader create(final RenderBridge bridge, final String vertexSource, final String fragmentSource, final BlendState blend) {
		final ByteBuffer vertex;
		final ByteBuffer fragment;
		try {
			vertex = ShaderCompiler.compileVertex(vertexSource);
			fragment = ShaderCompiler.compileFragment(fragmentSource);
		} catch (final IllegalStateException e) {
			System.err.println(e.getMessage());
			return new Shader(bridge, blend, false, 0L, 0L, 0L, 0L, new ArrayList<>(), new LinkedHashMap<>(), new HashMap<>(), new HashMap<>());
		}

		final ShaderReflection vertexReflection = ShaderReflection.reflect(vertex);
		final ShaderReflection fragmentReflection = ShaderReflection.reflect(fragment);

		final Map<Integer, UniformBlock> blockMap = new TreeMap<>(vertexReflection.getBlockMap());
		fragmentReflection.getBlockMap().forEach(blockMap::putIfAbsent);

		final Map<Integer, String> samplerNameMap = new TreeMap<>();
		vertexReflection.getSamplerMap().forEach((name, binding) -> samplerNameMap.put(binding, name));
		fragmentReflection.getSamplerMap().forEach((name, binding) -> samplerNameMap.put(binding, name));

		final Map<String, Integer> samplerBindings = new LinkedHashMap<>();
		samplerNameMap.forEach((binding, name) -> samplerBindings.put(name, binding));

		final Map<String, UniformMember> memberMap = new HashMap<>();
		blockMap.values().forEach(block -> memberMap.putAll(block.getMemberMap()));

		final Context context = bridge.getContext();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(blockMap.size() + samplerBindings.size(), stack);
			for (final UniformBlock block : blockMap.values()) {
				bindings.get().binding(block.getBinding()).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			}
			for (final int binding : samplerBindings.values()) {
				bindings.get().binding(binding).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT | VK10.VK_SHADER_STAGE_FRAGMENT_BIT);
			}
			bindings.flip();

			final LongBuffer descriptorSetLayout = stack.mallocLong(1);
			Context.check(VK10.vkCreateDescriptorSetLayout(context.getDevice(), VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings), null, descriptorSetLayout), "vkCreateDescriptorSetLayout");

			final LongBuffer pipelineLayout = stack.mallocLong(1);
			Context.check(VK10.vkCreatePipelineLayout(context.getDevice(), VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(descriptorSetLayout), null, pipelineLayout), "vkCreatePipelineLayout");

			return new Shader(bridge, blend, true, Shader.createModule(stack, context, vertex), Shader.createModule(stack, context, fragment), descriptorSetLayout.get(0), pipelineLayout.get(0), new ArrayList<>(blockMap.values()), samplerBindings, memberMap, new HashMap<>());
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

	@Override
	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name) {
		return this.samplerMap.computeIfAbsent(name, key -> new SamplerUniform());
	}

	@Override
	public @NonNull BooleanUniform getBooleanUniform(final @NonNull String name) {
		return new BooleanUniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull IntUniform getIntUniform(final @NonNull String name) {
		return new IntUniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name) {
		return new FloatArrayUniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull FloatUniform getFloatUniform(final @NonNull String name) {
		return new FloatUniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull Float2Uniform getFloat2Uniform(final @NonNull String name) {
		return new Float2Uniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull Float3Uniform getFloat3Uniform(final @NonNull String name) {
		return new Float3Uniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull Float4Uniform getFloat4Uniform(final @NonNull String name) {
		return new Float4Uniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name) {
		return new FloatMatrixUniform(this.memberMap.get(name));
	}

	@Override
	public @NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name) {
		return new Float4ArrayUniform(this.memberMap.get(name));
	}

	private static long createModule(final MemoryStack stack, final Context context, final ByteBuffer code) {
		final LongBuffer module = stack.mallocLong(1);
		Context.check(VK10.vkCreateShaderModule(context.getDevice(), VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code), null, module), "vkCreateShaderModule");
		return module.get(0);
	}

}