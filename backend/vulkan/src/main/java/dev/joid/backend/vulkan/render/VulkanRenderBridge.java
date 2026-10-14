package dev.joid.backend.vulkan.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkViewport;

import dev.joid.backend.vulkan.host.IVulkanHost;
import dev.joid.backend.vulkan.render.buffer.FrameAllocator;
import dev.joid.backend.vulkan.render.descriptor.DescriptorCache;
import dev.joid.backend.vulkan.render.framebuffer.VulkanFrameBuffer;
import dev.joid.backend.vulkan.render.pipeline.PipelineCache;
import dev.joid.backend.vulkan.render.shader.VulkanShader;
import dev.joid.backend.vulkan.render.texture.IVulkanTexture;
import dev.joid.backend.vulkan.render.texture.VulkanTexture;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.DepthRange;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.SamplerBinding;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.PipelineKey;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import dev.joid.lib.bridge.render.vertex.VertexFill;
import lombok.Getter;
import lombok.NonNull;

public final class VulkanRenderBridge extends RenderBridge {

	@Getter private final IVulkanHost     host;
	@Getter private final VulkanContext   context;
	@Getter private final PipelineCache   pipelineCache;
	@Getter private final DescriptorCache descriptorCache;

	private final long[]                             samplers;
	private final long                               screenLoadPass;
	private final long                               screenClearPass;
	private final FrameAllocator[]                   vertexAllocators;
	private final FrameAllocator[]                   uniformAllocators;
	private final NavigableMap<Long, List<Runnable>> garbage;

	private long              frame;
	private int               passWidth;
	private long              depthView;
	private int               depthWidth;
	private int               passHeight;
	private long              depthImage;
	private int               depthHeight;
	private long              depthMemory;
	private boolean           passActive;
	private long              boundPipeline;
	private boolean           screenCleared;
	private long              screenFramebuffer;
	private VulkanFrameBuffer passTarget;
	private FrameAllocator    vertexAllocator;
	private FrameAllocator    uniformAllocator;

	public VulkanRenderBridge(final IVulkanHost host) {
		if (host.getFramesInFlight() < 1) {
			throw new IllegalArgumentException("The Vulkan host " + host.getClass().getSimpleName() + " has " + host.getFramesInFlight() + " frames in flight, getFramesInFlight() must return 1 or more");
		}

		this.host              = host;
		this.context           = new VulkanContext(host);
		this.pipelineCache     = new PipelineCache(this.context);
		this.descriptorCache   = new DescriptorCache(this.context);
		this.garbage           = new TreeMap<>();
		this.screenClearPass   = this.context.createRenderPass(host.getColorFormat(), this.context.getDepthStencilFormat(), true);
		this.screenLoadPass    = this.context.createRenderPass(host.getColorFormat(), this.context.getDepthStencilFormat(), false);
		this.vertexAllocators  = new FrameAllocator[host.getFramesInFlight()];
		this.uniformAllocators = new FrameAllocator[host.getFramesInFlight()];
		for (int i = 0; i < host.getFramesInFlight(); i++) {
			this.vertexAllocators[i] = new FrameAllocator(this.context, 8L << 20, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 1L, this::dispose);
			this.uniformAllocators[i] = new FrameAllocator(this.context, 8L << 20, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, this.context.getUniformAlignment(), this::dispose);
		}

		this.vertexAllocator  = this.vertexAllocators[0];
		this.uniformAllocator = this.uniformAllocators[0];
		this.samplers         = this.createSamplers();
	}

	public void destroy() {
		final VkDevice device = this.context.getDevice();
		VK10.vkDeviceWaitIdle(device);
		this.releaseScreenTarget();
		this.releaseGarbage(Long.MAX_VALUE);
		for (int i = 0; i < this.vertexAllocators.length; i++) {
			this.vertexAllocators[i].destroy();
			this.uniformAllocators[i].destroy();
		}

		for (final long sampler : this.samplers) {
			VK10.vkDestroySampler(device, sampler, null);
		}

		this.pipelineCache.destroy();
		this.descriptorCache.destroy();
		VK10.vkDestroyRenderPass(device, this.screenClearPass, null);
		VK10.vkDestroyRenderPass(device, this.screenLoadPass, null);
		this.context.destroy();
	}

	public void dispose(final @NonNull Runnable destroyer) {
		this.garbage.computeIfAbsent(this.frame, frame -> new ArrayList<>()).add(destroyer);
	}

	public void record(final @NonNull Consumer<VkCommandBuffer> recorder) {
		if (!super.isFrameActive()) {
			this.context.submit(recorder);
			return;
		}

		this.endPass();
		recorder.accept(this.host.getCommandBuffer());
	}

	@Override
	protected void beginFrameCommands() {
		this.frame++;
		this.releaseGarbage(this.frame - this.host.getFramesInFlight());

		this.vertexAllocator  = this.vertexAllocators[this.host.getFrameIndex()];
		this.uniformAllocator = this.uniformAllocators[this.host.getFrameIndex()];
		this.vertexAllocator.reset();
		this.uniformAllocator.reset();
		this.screenCleared = false;
		this.passActive    = false;
		this.passTarget    = null;
		this.boundPipeline = VK10.VK_NULL_HANDLE;
	}

	@Override
	protected void submitFrameCommands() {
		this.endPass();
		if (this.screenFramebuffer != VK10.VK_NULL_HANDLE) {
			final VkDevice device = this.context.getDevice();
			final long framebuffer = this.screenFramebuffer;
			this.dispose(() -> VK10.vkDestroyFramebuffer(device, framebuffer, null));
			this.screenFramebuffer = VK10.VK_NULL_HANDLE;
		}
	}

	@Override
	public void suspend(final @NonNull Runnable draw) {
		this.endPass();
		try {
			draw.run();
		} finally {
			this.boundPipeline = VK10.VK_NULL_HANDLE;
		}
	}

	@Override
	public void raster(final @NonNull IFrameBuffer target, final int width, final int height, final @NonNull Runnable draw) {
		super.requireFrame();
		this.beginPass((VulkanFrameBuffer) target);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkCommandBuffer buffer = this.host.getCommandBuffer();
			VK10.vkCmdSetViewport(buffer, 0, VkViewport.calloc(1, stack).width(width).height(height).minDepth(0F).maxDepth(1F));
			final VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.extent().set(width, height);
			VK10.vkCmdSetScissor(buffer, 0, scissor);
		}

		try {
			draw.run();
		} finally {
			this.boundPipeline = VK10.VK_NULL_HANDLE;
		}
	}

	@Override
	protected void clearColorBuffer(final float red, final float green, final float blue, final float alpha) {
		super.requireFrame();
		this.beginPass((VulkanFrameBuffer) super.getState().getFrameBuffer());
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).colorAttachment(0);
			attachment.clearValue().color().float32(0, red).float32(1, green).float32(2, blue).float32(3, alpha);
			VK10.vkCmdClearAttachments(this.host.getCommandBuffer(), attachment, this.createClearRect(stack));
		}
	}

	@Override
	protected void clearDepthBuffer() {
		super.requireFrame();
		this.beginPass((VulkanFrameBuffer) super.getState().getFrameBuffer());
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT);
			attachment.clearValue().depthStencil().depth(1F);
			VK10.vkCmdClearAttachments(this.host.getCommandBuffer(), attachment, this.createClearRect(stack));
		}
	}

	@Override
	protected void clearStencilBuffer() {
		super.requireFrame();
		this.beginPass((VulkanFrameBuffer) super.getState().getFrameBuffer());
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
			attachment.clearValue().depthStencil().stencil(0);
			VK10.vkCmdClearAttachments(this.host.getCommandBuffer(), attachment, this.createClearRect(stack));
		}
	}

	@Override
	protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader current) {
		super.requireFrame();
		final RenderState state = super.getState();
		final VulkanShader shader = (VulkanShader) current;
		final VulkanFrameBuffer target = (VulkanFrameBuffer) state.getFrameBuffer();
		this.beginPass(target);

		final long pipeline = this.pipelineCache.get(PipelineKey.create(shader, state, primitive), target != null, target == null ? this.screenClearPass : this.context.getOffscreenRenderPass());
		if (pipeline != this.boundPipeline) {
			VK10.vkCmdBindPipeline(this.host.getCommandBuffer(), VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
			this.boundPipeline = pipeline;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			this.applyDynamicState(stack, state, target != null, primitive == Primitive.LINES ? VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST : VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
			final long vertexOffset = this.writeVertices(buffer, state);
			final IntBuffer dynamicOffsets = this.writeUniforms(stack, state, shader, buffer);
			final long descriptorSet = this.descriptorCache.get(shader, this.uniformAllocator.getBuffer().getBuffer(), this.getImages(shader));

			VK10.vkCmdBindDescriptorSets(this.host.getCommandBuffer(), VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, shader.getPipelineLayout(), 0, stack.longs(descriptorSet), dynamicOffsets);
			VK10.vkCmdBindVertexBuffers(this.host.getCommandBuffer(), 0, stack.longs(this.vertexAllocator.getBuffer().getBuffer()), stack.longs(vertexOffset));
			VK10.vkCmdDraw(this.host.getCommandBuffer(), buffer.getCount(), 1, 0, 0);
		}
	}

	@Override
	public @NonNull ITexture createTexture() {
		return new VulkanTexture(this);
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height) {
		return VulkanFrameBuffer.create(this, width, height);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return VulkanShader.create(this, vertex, fragment, blend);
	}

	public void releaseHandle(final long handle) {
		this.descriptorCache.invalidate(handle, this::dispose);
	}

	private void endPass() {
		if (this.passActive) {
			VK10.vkCmdEndRenderPass(this.host.getCommandBuffer());
			if (this.passTarget != null) {
				VulkanContext.transition(this.host.getCommandBuffer(), this.passTarget.getTexture().getImage(), VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}
			this.passActive = false;
		}
	}

	private void beginPass(final VulkanFrameBuffer target) {
		if (this.passActive && this.passTarget == target) {
			return;
		}

		this.endPass();
		final VkCommandBuffer buffer = this.host.getCommandBuffer();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkRenderPassBeginInfo info = VkRenderPassBeginInfo.calloc(stack).sType$Default();
			if (target == null) {
				this.passWidth = this.host.getTargetWidth();
				this.passHeight = this.host.getTargetHeight();
				if (this.screenFramebuffer == VK10.VK_NULL_HANDLE) {
					this.screenFramebuffer = this.createScreenFramebuffer(stack);
				}

				info.renderPass(this.screenCleared ? this.screenLoadPass : this.screenClearPass).framebuffer(this.screenFramebuffer);
				if (!this.screenCleared) {
					final VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
					clearValues.get(1).depthStencil().depth(1F).stencil(0);
					info.pClearValues(clearValues);
				}
				this.screenCleared = true;
			} else {
				this.passWidth = target.getWidth();
				this.passHeight = target.getHeight();
				VulkanContext.transition(buffer, target.getTexture().getImage(), VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
				info.renderPass(this.context.getOffscreenRenderPass()).framebuffer(target.getFramebuffer());
			}

			info.renderArea().extent().set(this.passWidth, this.passHeight);
			VK10.vkCmdBeginRenderPass(buffer, info, VK10.VK_SUBPASS_CONTENTS_INLINE);
		}

		this.passActive    = true;
		this.passTarget    = target;
		this.boundPipeline = VK10.VK_NULL_HANDLE;
	}

	private long createScreenFramebuffer(final MemoryStack stack) {
		final int format = this.context.getDepthStencilFormat();
		if (this.depthWidth != this.passWidth || this.depthHeight != this.passHeight) {
			this.releaseScreenTarget();
			final long[] depth = this.context.createImage(this.passWidth, this.passHeight, format, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
			this.depthImage  = depth[0];
			this.depthMemory = depth[1];
			this.depthView   = this.context.createImageView(this.depthImage, format, VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
			this.depthWidth  = this.passWidth;
			this.depthHeight = this.passHeight;
		}

		final LongBuffer framebuffer = stack.mallocLong(1);
		VulkanContext.check(VK10.vkCreateFramebuffer(this.context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(this.screenClearPass).pAttachments(stack.longs(this.host.getTargetView(), this.depthView)).width(this.passWidth).height(this.passHeight).layers(1), null, framebuffer), "vkCreateFramebuffer");
		return framebuffer.get(0);
	}

	private void releaseScreenTarget() {
		if (this.depthImage == VK10.VK_NULL_HANDLE) {
			return;
		}

		final VkDevice device = this.context.getDevice();
		final long view = this.depthView;
		final long image = this.depthImage;
		final long memory = this.depthMemory;
		this.dispose(() -> {
			VK10.vkDestroyImageView(device, view, null);
			VK10.vkDestroyImage(device, image, null);
			VK10.vkFreeMemory(device, memory, null);
		});

		this.depthView   = VK10.VK_NULL_HANDLE;
		this.depthImage  = VK10.VK_NULL_HANDLE;
		this.depthMemory = VK10.VK_NULL_HANDLE;
		this.depthWidth  = 0;
		this.depthHeight = 0;
	}

	private void releaseGarbage(final long lastFrame) {
		final NavigableMap<Long, List<Runnable>> released = this.garbage.headMap(lastFrame, true);
		final List<Runnable> destroyers = new ArrayList<>();
		released.values().forEach(destroyers::addAll);
		released.clear();
		destroyers.forEach(Runnable::run);
	}

	private VkClearRect.Buffer createClearRect(final MemoryStack stack) {
		final VkClearRect.Buffer rect = VkClearRect.calloc(1, stack).baseArrayLayer(0).layerCount(1);
		rect.rect().extent().set(this.passWidth, this.passHeight);
		return rect;
	}

	private void applyDynamicState(final MemoryStack stack, final RenderState state, final boolean offscreen, final int topology) {
		final VkCommandBuffer buffer = this.host.getCommandBuffer();
		final VkViewport.Buffer viewport = VkViewport.calloc(1, stack).x(state.getViewportX()).width(state.getViewportWidth()).minDepth(0F).maxDepth(1F);
		if (offscreen) {
			viewport.y(state.getViewportY()).height(state.getViewportHeight());
		} else {
			viewport.y(this.passHeight - state.getViewportY()).height(-state.getViewportHeight());
		}
		VK10.vkCmdSetViewport(buffer, 0, viewport);

		final VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
		scissor.extent().set(this.passWidth, this.passHeight);
		VK10.vkCmdSetScissor(buffer, 0, scissor);

		VK13.vkCmdSetPrimitiveTopology(buffer, topology);
		VK13.vkCmdSetCullMode(buffer, state.isCull() ? VK10.VK_CULL_MODE_BACK_BIT : VK10.VK_CULL_MODE_NONE);
		VK13.vkCmdSetFrontFace(buffer, offscreen ? VK10.VK_FRONT_FACE_CLOCKWISE : VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE);
		VK13.vkCmdSetDepthTestEnable(buffer, state.isDepthTest());
		VK13.vkCmdSetDepthWriteEnable(buffer, state.isDepthWrite());
		VK13.vkCmdSetDepthCompareOp(buffer, VK10.VK_COMPARE_OP_LESS);
		final StencilState stencil = state.getStencil();
		VK13.vkCmdSetStencilTestEnable(buffer, stencil.isEnabled() && !offscreen);
		VK13.vkCmdSetStencilOp(buffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, VulkanRenderBridge.operation(stencil.getFail()), VulkanRenderBridge.operation(stencil.getPass()), VulkanRenderBridge.operation(stencil.getDepthFail()), VulkanRenderBridge.compare(stencil.getFunction()));
		VK10.vkCmdSetStencilCompareMask(buffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, stencil.getMask());
		VK10.vkCmdSetStencilWriteMask(buffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, 0xFF);
		VK10.vkCmdSetStencilReference(buffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, stencil.getReference());
	}

	private long writeVertices(final VertexBuffer buffer, final RenderState state) {
		final int size = buffer.getCount() * VertexBuffer.STRIDE;
		final long offset = this.vertexAllocator.allocate(size);
		VertexFill.complete(buffer, MemoryUtil.memByteBuffer(this.vertexAllocator.getBuffer().getAddress() + offset, size));
		return offset;
	}

	private IntBuffer writeUniforms(final MemoryStack stack, final RenderState state, final VulkanShader shader, final VertexBuffer buffer) {
		shader.builtins(state, buffer, DepthRange.toZeroToOne(super.getProjection().getMatrix()), super.getModelView()).pack();

		final long previousBuffer = this.uniformAllocator.getBuffer().getBuffer();
		final IntBuffer offsets = this.uploadBlock(stack, shader);
		if (this.uniformAllocator.getBuffer().getBuffer() == previousBuffer) {
			return offsets;
		}

		this.descriptorCache.invalidate(previousBuffer, this::dispose);
		return this.uploadBlock(stack, shader);
	}

	private IntBuffer uploadBlock(final MemoryStack stack, final VulkanShader shader) {
		final ByteBuffer data = shader.getBlock().getData();
		final long offset = this.uniformAllocator.allocate(data.capacity());
		MemoryUtil.memCopy(MemoryUtil.memAddress(data), this.uniformAllocator.getBuffer().getAddress() + offset, data.capacity());
		return stack.ints((int) offset);
	}

	private long[] getImages(final VulkanShader shader) {
		final long[] images = new long[shader.getSamplerMap().size() * 2];
		int index = 0;
		for (final UniformSampler sampler : shader.getSamplerMap().values()) {
			final SamplerBinding binding = super.resolveSampler(sampler);
			images[index] = ((IVulkanTexture) binding.getTexture()).getView();
			images[index + 1] = this.samplers[binding.getSampling().getIndex()];
			index += 2;
		}
		return images;
	}

	private long[] createSamplers() {
		final long[] samplers = new long[TextureSampling.values().size()];
		try (MemoryStack stack = MemoryStack.stackPush()) {
			for (final TextureSampling sampling : TextureSampling.values()) {
				samplers[sampling.getIndex()] = this.createSampler(stack, sampling);
			}
		}
		return samplers;
	}

	private long createSampler(final MemoryStack stack, final TextureSampling sampling) {
		final int addressMode = VulkanRenderBridge.addressMode(sampling.getWrap());
		final int filterMode = sampling.getFilter() == TextureFilter.LINEAR ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST;
		final VkSamplerCreateInfo info = VkSamplerCreateInfo.calloc(stack)
				.sType$Default()
				.magFilter(filterMode)
				.minFilter(filterMode)
				.mipmapMode(sampling.isMipmapFiltered() ? VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR : VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.addressModeU(addressMode)
				.addressModeV(addressMode)
				.addressModeW(addressMode)
				.maxLod(sampling.isMipmapFiltered() ? VK10.VK_LOD_CLAMP_NONE : 0F)
				.borderColor(VK10.VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK);

		final LongBuffer sampler = stack.mallocLong(1);
		VulkanContext.check(VK10.vkCreateSampler(this.context.getDevice(), info, null, sampler), "vkCreateSampler");
		return sampler.get(0);
	}

	private static int addressMode(final TextureWrap wrap) {
		switch (wrap) {
		case CLAMP_TO_EDGE:
			return VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
		case CLAMP_TO_BORDER:
			return VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
		default:
			return VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
		}
	}

	private static int compare(final StencilFunction function) {
		switch (function) {
		case NEVER:
			return VK10.VK_COMPARE_OP_NEVER;
		case LESS:
			return VK10.VK_COMPARE_OP_LESS;
		case LESS_EQUAL:
			return VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
		case GREATER:
			return VK10.VK_COMPARE_OP_GREATER;
		case GREATER_EQUAL:
			return VK10.VK_COMPARE_OP_GREATER_OR_EQUAL;
		case EQUAL:
			return VK10.VK_COMPARE_OP_EQUAL;
		case NOT_EQUAL:
			return VK10.VK_COMPARE_OP_NOT_EQUAL;
		default:
			return VK10.VK_COMPARE_OP_ALWAYS;
		}
	}

	private static int operation(final StencilOperation operation) {
		switch (operation) {
		case ZERO:
			return VK10.VK_STENCIL_OP_ZERO;
		case REPLACE:
			return VK10.VK_STENCIL_OP_REPLACE;
		case INCREMENT:
			return VK10.VK_STENCIL_OP_INCREMENT_AND_CLAMP;
		case DECREMENT:
			return VK10.VK_STENCIL_OP_DECREMENT_AND_CLAMP;
		case INVERT:
			return VK10.VK_STENCIL_OP_INVERT;
		default:
			return VK10.VK_STENCIL_OP_KEEP;
		}
	}

}