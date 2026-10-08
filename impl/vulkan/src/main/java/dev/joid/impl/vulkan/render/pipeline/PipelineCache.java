package dev.joid.impl.vulkan.render.pipeline;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTLineRasterization;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationLineStateCreateInfoEXT;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import dev.joid.impl.vulkan.render.Context;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public final class PipelineCache {

	private final Context                context;
	private final Map<PipelineKey, Long> pipelineMap;

	public PipelineCache(final Context context) {
		this.context     = context;
		this.pipelineMap = new HashMap<>();
	}

	public long get(final @NonNull PipelineKey key, final long renderPass) {
		return this.pipelineMap.computeIfAbsent(key, pipelineKey -> this.create(pipelineKey, renderPass));
	}

	private long create(final PipelineKey key, final long renderPass) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final ByteBuffer entryPoint = stack.UTF8("main");
			final VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
			stages.get(0).sType$Default().stage(VK10.VK_SHADER_STAGE_VERTEX_BIT).module(key.getShader().getVertexModule()).pName(entryPoint);
			stages.get(1).sType$Default().stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT).module(key.getShader().getFragmentModule()).pName(entryPoint);

			final VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(VertexAttribute.values().length, stack);
			for (final VertexAttribute attribute : VertexAttribute.values()) {
				attributes.get(attribute.ordinal()).location(attribute.getLocation()).binding(0).format(PipelineCache.format(attribute)).offset(attribute.getOffset());
			}

			final VkPipelineVertexInputStateCreateInfo vertexInput = VkPipelineVertexInputStateCreateInfo.calloc(stack)
					.sType$Default()
					.pVertexBindingDescriptions(VkVertexInputBindingDescription.calloc(1, stack).binding(0).stride(VertexBuffer.STRIDE).inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX))
					.pVertexAttributeDescriptions(attributes);

			final VkPipelineRasterizationStateCreateInfo rasterization = VkPipelineRasterizationStateCreateInfo.calloc(stack)
					.sType$Default()
					.polygonMode(VK10.VK_POLYGON_MODE_FILL)
					.lineWidth(1F)
					.cullMode(VK10.VK_CULL_MODE_NONE)
					.frontFace(VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE);
			if (key.isSmooth() && this.context.isSmoothLines()) {
				rasterization.pNext(VkPipelineRasterizationLineStateCreateInfoEXT.calloc(stack).sType$Default().lineRasterizationMode(EXTLineRasterization.VK_LINE_RASTERIZATION_MODE_RECTANGULAR_SMOOTH_EXT).address());
			}

			final BlendState blend = key.getBlend();
			final VkPipelineColorBlendAttachmentState.Buffer blendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack)
					.blendEnable(blend.isEnabled())
					.srcColorBlendFactor(PipelineCache.factor(blend.getSourceColor()))
					.dstColorBlendFactor(PipelineCache.factor(blend.getDestinationColor()))
					.colorBlendOp(PipelineCache.operation(blend.getEquation()))
					.srcAlphaBlendFactor(PipelineCache.factor(blend.getSourceAlpha()))
					.dstAlphaBlendFactor(PipelineCache.factor(blend.getDestinationAlpha()))
					.alphaBlendOp(PipelineCache.operation(blend.getEquation()))
					.colorWriteMask(key.isColorMask() ? VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT : 0);

			final VkGraphicsPipelineCreateInfo.Buffer info = VkGraphicsPipelineCreateInfo.calloc(1, stack)
					.sType$Default()
					.pStages(stages)
					.pVertexInputState(vertexInput)
					.pInputAssemblyState(VkPipelineInputAssemblyStateCreateInfo.calloc(stack).sType$Default().topology(key.isLines() ? VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST : VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST))
					.pViewportState(VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().viewportCount(1).scissorCount(1))
					.pRasterizationState(rasterization)
					.pMultisampleState(VkPipelineMultisampleStateCreateInfo.calloc(stack).sType$Default().rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT))
					.pDepthStencilState(VkPipelineDepthStencilStateCreateInfo.calloc(stack).sType$Default().depthCompareOp(VK10.VK_COMPARE_OP_LESS))
					.pColorBlendState(VkPipelineColorBlendStateCreateInfo.calloc(stack).sType$Default().pAttachments(blendAttachment))
					.pDynamicState(VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default().pDynamicStates(stack.ints(VK10.VK_DYNAMIC_STATE_VIEWPORT, VK10.VK_DYNAMIC_STATE_SCISSOR, VK10.VK_DYNAMIC_STATE_LINE_WIDTH, VK10.VK_DYNAMIC_STATE_STENCIL_COMPARE_MASK, VK10.VK_DYNAMIC_STATE_STENCIL_WRITE_MASK, VK10.VK_DYNAMIC_STATE_STENCIL_REFERENCE, VK13.VK_DYNAMIC_STATE_CULL_MODE, VK13.VK_DYNAMIC_STATE_FRONT_FACE, VK13.VK_DYNAMIC_STATE_PRIMITIVE_TOPOLOGY, VK13.VK_DYNAMIC_STATE_DEPTH_TEST_ENABLE, VK13.VK_DYNAMIC_STATE_DEPTH_WRITE_ENABLE, VK13.VK_DYNAMIC_STATE_DEPTH_COMPARE_OP, VK13.VK_DYNAMIC_STATE_STENCIL_TEST_ENABLE, VK13.VK_DYNAMIC_STATE_STENCIL_OP)))
					.layout(key.getShader().getPipelineLayout())
					.renderPass(renderPass)
					.subpass(0);

			final LongBuffer pipeline = stack.mallocLong(1);
			Context.check(VK10.vkCreateGraphicsPipelines(this.context.getDevice(), VK10.VK_NULL_HANDLE, info, null, pipeline), "vkCreateGraphicsPipelines");
			return pipeline.get(0);
		}
	}

	private static int format(final VertexAttribute attribute) {
		switch (attribute) {
		case TEXTURE_COORDINATE:
			return VK10.VK_FORMAT_R32G32_SFLOAT;
		case COLOR:
			return VK10.VK_FORMAT_R8G8B8A8_UNORM;
		case NORMAL:
			return VK10.VK_FORMAT_R8G8B8A8_SNORM;
		default:
			return VK10.VK_FORMAT_R32G32B32_SFLOAT;
		}
	}

	private static int operation(final BlendState.Equation equation) {
		switch (equation) {
		case SUBTRACT:
			return VK10.VK_BLEND_OP_SUBTRACT;
		case REVERSE_SUBTRACT:
			return VK10.VK_BLEND_OP_REVERSE_SUBTRACT;
		case MIN:
			return VK10.VK_BLEND_OP_MIN;
		case MAX:
			return VK10.VK_BLEND_OP_MAX;
		default:
			return VK10.VK_BLEND_OP_ADD;
		}
	}

	private static int factor(final BlendState.Factor factor) {
		switch (factor) {
		case ZERO:
			return VK10.VK_BLEND_FACTOR_ZERO;
		case SRC_COLOR:
			return VK10.VK_BLEND_FACTOR_SRC_COLOR;
		case ONE_MINUS_SRC_COLOR:
			return VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR;
		case DST_COLOR:
			return VK10.VK_BLEND_FACTOR_DST_COLOR;
		case ONE_MINUS_DST_COLOR:
			return VK10.VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR;
		case SRC_ALPHA:
			return VK10.VK_BLEND_FACTOR_SRC_ALPHA;
		case ONE_MINUS_SRC_ALPHA:
			return VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
		case DST_ALPHA:
			return VK10.VK_BLEND_FACTOR_DST_ALPHA;
		case ONE_MINUS_DST_ALPHA:
			return VK10.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA;
		default:
			return VK10.VK_BLEND_FACTOR_ONE;
		}
	}

}