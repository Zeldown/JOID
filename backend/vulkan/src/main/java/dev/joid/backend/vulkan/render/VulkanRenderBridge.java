package dev.joid.backend.vulkan.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

import dev.joid.backend.vulkan.render.buffer.Buffer;
import dev.joid.backend.vulkan.render.buffer.FrameAllocator;
import dev.joid.backend.vulkan.render.descriptor.DescriptorCache;
import dev.joid.backend.vulkan.render.framebuffer.VulkanFrameBuffer;
import dev.joid.backend.vulkan.render.pipeline.PipelineCache;
import dev.joid.backend.vulkan.render.shader.VulkanShader;
import dev.joid.backend.vulkan.render.texture.VulkanBorrowedTexture;
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

@Getter
public final class VulkanRenderBridge extends RenderBridge {

	private final long            frameFence;
	private final long[]          samplers;
	private final Swapchain       swapchain;
	private final long            imageSemaphore;
	private final VulkanContext   context;
	private final List<Runnable>  garbage;
	private final PipelineCache   pipelineCache;
	private final VkCommandBuffer commandBuffer;
	private final FrameAllocator  vertexAllocator;
	private final FrameAllocator  uniformAllocator;
	private final DescriptorCache descriptorCache;

	private int               passWidth;
	private int               imageIndex;
	private int               passHeight;
	private boolean           passActive;
	private long              boundPipeline;
	private Buffer            stagingBuffer;
	private boolean           screenCleared;
	private VulkanFrameBuffer passTarget;

	public VulkanRenderBridge(final long window) {
		this.context          = new VulkanContext(window);
		this.swapchain        = new Swapchain(this.context, window);
		this.pipelineCache    = new PipelineCache(this.context);
		this.descriptorCache  = new DescriptorCache(this.context);
		this.garbage          = new ArrayList<>();
		this.vertexAllocator  = new FrameAllocator(this.context, 8L << 20, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 1L, this::dispose);
		this.uniformAllocator = new FrameAllocator(this.context, 8L << 20, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, this.context.getUniformAlignment(), this::dispose);
		this.commandBuffer    = this.context.allocateCommandBuffer();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer semaphore = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateSemaphore(this.context.getDevice(), VkSemaphoreCreateInfo.calloc(stack).sType$Default(), null, semaphore), "vkCreateSemaphore");
			this.imageSemaphore = semaphore.get(0);

			final LongBuffer fence = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateFence(this.context.getDevice(), VkFenceCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT), null, fence), "vkCreateFence");
			this.frameFence = fence.get(0);
		}

		this.samplers = this.createSamplers();
	}

	public void dispose(final @NonNull Runnable destroyer) {
		if (super.isFrameActive()) {
			this.garbage.add(destroyer);
		} else {
			destroyer.run();
		}
	}

	@Override
	protected void submitFrameCommands() {
		if (!this.screenCleared) {
			this.beginPass(null);
		}

		this.endPass();
		VulkanContext.transition(this.commandBuffer, this.swapchain.getImages()[this.imageIndex], VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		VulkanContext.check(VK10.vkEndCommandBuffer(this.commandBuffer), "vkEndCommandBuffer");

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkSubmitInfo submit = VkSubmitInfo.calloc(stack)
					.sType$Default()
					.waitSemaphoreCount(1)
					.pWaitSemaphores(stack.longs(this.imageSemaphore))
					.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
					.pCommandBuffers(stack.pointers(this.commandBuffer));
			VulkanContext.check(VK10.vkResetFences(this.context.getDevice(), this.frameFence), "vkResetFences");
			VulkanContext.check(VK10.vkQueueSubmit(this.context.getQueue(), submit, this.frameFence), "vkQueueSubmit");
			VulkanContext.check(VK10.vkWaitForFences(this.context.getDevice(), this.frameFence, true, -1L), "vkWaitForFences");
		}

		final List<Runnable> released = new ArrayList<>(this.garbage);
		this.garbage.clear();
		released.forEach(Runnable::run);
	}

	@Override
	protected void beginFrameCommands() {
		if (this.swapchain.isOutdated()) {
			this.swapchain.recreate();
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer index = stack.mallocInt(1);
			int result = KHRSwapchain.vkAcquireNextImageKHR(this.context.getDevice(), this.swapchain.getSwapchain(), -1L, this.imageSemaphore, VK10.VK_NULL_HANDLE, index);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
				this.swapchain.recreate();
				result = KHRSwapchain.vkAcquireNextImageKHR(this.context.getDevice(), this.swapchain.getSwapchain(), -1L, this.imageSemaphore, VK10.VK_NULL_HANDLE, index);
			}

			if (result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				VulkanContext.check(result, "vkAcquireNextImageKHR");
			}

			this.imageIndex = index.get(0);
			VulkanContext.check(VK10.vkResetCommandBuffer(this.commandBuffer, 0), "vkResetCommandBuffer");
			VulkanContext.check(VK10.vkBeginCommandBuffer(this.commandBuffer, VkCommandBufferBeginInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "vkBeginCommandBuffer");
		}

		this.vertexAllocator.reset();
		this.uniformAllocator.reset();
		this.screenCleared = false;
		this.passActive    = false;
		this.passTarget    = null;
		this.boundPipeline = VK10.VK_NULL_HANDLE;
	}

	public void present() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkPresentInfoKHR info = VkPresentInfoKHR.calloc(stack)
					.sType$Default()
					.swapchainCount(1)
					.pSwapchains(stack.longs(this.swapchain.getSwapchain()))
					.pImageIndices(stack.ints(this.imageIndex));
			final int result = KHRSwapchain.vkQueuePresentKHR(this.context.getQueue(), info);
			if (result != KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				VulkanContext.check(result, "vkQueuePresentKHR");
			}
		}
	}

	@Override
	protected void clearColorBuffer(final float red, final float green, final float blue, final float alpha) {
		super.requireFrame();
		this.beginPass((VulkanFrameBuffer) super.getState().getFrameBuffer());
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).colorAttachment(0);
			attachment.clearValue().color().float32(0, red).float32(1, green).float32(2, blue).float32(3, alpha);
			VK10.vkCmdClearAttachments(this.commandBuffer, attachment, this.createClearRect(stack));
		}
	}

	@Override
	protected void clearDepthBuffer() {
		super.requireFrame();
		this.beginPass((VulkanFrameBuffer) super.getState().getFrameBuffer());
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT);
			attachment.clearValue().depthStencil().depth(1F);
			VK10.vkCmdClearAttachments(this.commandBuffer, attachment, this.createClearRect(stack));
		}
	}

	@Override
	protected void clearStencilBuffer() {
		super.requireFrame();
		this.beginPass(null);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
			attachment.clearValue().depthStencil().stencil(0);
			VK10.vkCmdClearAttachments(this.commandBuffer, attachment, this.createClearRect(stack));
		}
	}

	@Override
	protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader current) {
		super.requireFrame();
		final RenderState state = super.getState();
		final VulkanShader shader = (VulkanShader) current;
		final VulkanFrameBuffer target = (VulkanFrameBuffer) state.getFrameBuffer();
		this.beginPass(target);

		final long pipeline = this.pipelineCache.get(PipelineKey.create(shader, state, primitive), target != null, target == null ? this.swapchain.getClearRenderPass() : this.context.getOffscreenRenderPass());
		if (pipeline != this.boundPipeline) {
			VK10.vkCmdBindPipeline(this.commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
			this.boundPipeline = pipeline;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			this.applyDynamicState(stack, state, target != null, primitive == Primitive.LINES ? VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST : VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
			final long vertexOffset = this.writeVertices(buffer, state);
			final IntBuffer dynamicOffsets = this.writeUniforms(stack, state, shader, buffer);
			final long descriptorSet = this.descriptorCache.get(shader, this.uniformAllocator.getBuffer().getBuffer(), this.getImages(shader));

			VK10.vkCmdBindDescriptorSets(this.commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, shader.getPipelineLayout(), 0, stack.longs(descriptorSet), dynamicOffsets);
			VK10.vkCmdBindVertexBuffers(this.commandBuffer, 0, stack.longs(this.vertexAllocator.getBuffer().getBuffer()), stack.longs(vertexOffset));
			VK10.vkCmdDraw(this.commandBuffer, buffer.getCount(), 1, 0, 0);
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

	public @NonNull Buffer getStagingBuffer(final long size) {
		if (this.stagingBuffer == null || this.stagingBuffer.getSize() < size) {
			if (this.stagingBuffer != null) {
				this.stagingBuffer.destroy();
			}

			this.stagingBuffer = Buffer.create(this.context, Math.max(size, 4L << 20), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		}
		return this.stagingBuffer;
	}

	private void endPass() {
		if (this.passActive) {
			VK10.vkCmdEndRenderPass(this.commandBuffer);
			if (this.passTarget != null) {
				VulkanContext.transition(this.commandBuffer, this.passTarget.getTexture().getImage(), VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}
			this.passActive = false;
		}
	}

	private void beginPass(final VulkanFrameBuffer target) {
		if (this.passActive && this.passTarget == target) {
			return;
		}

		this.endPass();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkRenderPassBeginInfo info = VkRenderPassBeginInfo.calloc(stack).sType$Default();
			if (target == null) {
				this.passWidth = this.swapchain.getWidth();
				this.passHeight = this.swapchain.getHeight();
				info.renderPass(this.screenCleared ? this.swapchain.getLoadRenderPass() : this.swapchain.getClearRenderPass()).framebuffer(this.swapchain.getFramebuffers()[this.imageIndex]);
				if (!this.screenCleared) {
					final VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
					clearValues.get(1).depthStencil().depth(1F).stencil(0);
					info.pClearValues(clearValues);
				}
				this.screenCleared = true;
			} else {
				this.passWidth = target.getWidth();
				this.passHeight = target.getHeight();
				VulkanContext.transition(this.commandBuffer, target.getTexture().getImage(), VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
				final VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
				clearValues.get(1).depthStencil().depth(1F).stencil(0);
				info.renderPass(this.context.getOffscreenRenderPass()).framebuffer(target.getFramebuffer()).pClearValues(clearValues);
			}

			info.renderArea().extent().set(this.passWidth, this.passHeight);
			VK10.vkCmdBeginRenderPass(this.commandBuffer, info, VK10.VK_SUBPASS_CONTENTS_INLINE);
		}

		this.passActive    = true;
		this.passTarget    = target;
		this.boundPipeline = VK10.VK_NULL_HANDLE;
	}

	private VkClearRect.Buffer createClearRect(final MemoryStack stack) {
		final VkClearRect.Buffer rect = VkClearRect.calloc(1, stack).baseArrayLayer(0).layerCount(1);
		rect.rect().extent().set(this.passWidth, this.passHeight);
		return rect;
	}

	private void applyDynamicState(final MemoryStack stack, final RenderState state, final boolean offscreen, final int topology) {
		final VkViewport.Buffer viewport = VkViewport.calloc(1, stack).x(state.getViewportX()).width(state.getViewportWidth()).minDepth(0F).maxDepth(1F);
		if (offscreen) {
			viewport.y(state.getViewportY()).height(state.getViewportHeight());
		} else {
			viewport.y(this.passHeight - state.getViewportY()).height(-state.getViewportHeight());
		}
		VK10.vkCmdSetViewport(this.commandBuffer, 0, viewport);

		final VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
		scissor.extent().set(this.passWidth, this.passHeight);
		VK10.vkCmdSetScissor(this.commandBuffer, 0, scissor);

		VK13.vkCmdSetPrimitiveTopology(this.commandBuffer, topology);
		VK13.vkCmdSetCullMode(this.commandBuffer, state.isCull() ? VK10.VK_CULL_MODE_BACK_BIT : VK10.VK_CULL_MODE_NONE);
		VK13.vkCmdSetFrontFace(this.commandBuffer, offscreen ? VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE : VK10.VK_FRONT_FACE_CLOCKWISE);
		VK13.vkCmdSetDepthTestEnable(this.commandBuffer, state.isDepthTest());
		VK13.vkCmdSetDepthWriteEnable(this.commandBuffer, state.isDepthWrite());
		VK13.vkCmdSetDepthCompareOp(this.commandBuffer, VK10.VK_COMPARE_OP_LESS);
		final StencilState stencil = state.getStencil();
		VK13.vkCmdSetStencilTestEnable(this.commandBuffer, stencil.isEnabled() && !offscreen);
		VK13.vkCmdSetStencilOp(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, VulkanRenderBridge.operation(stencil.getFail()), VulkanRenderBridge.operation(stencil.getPass()), VulkanRenderBridge.operation(stencil.getDepthFail()), VulkanRenderBridge.compare(stencil.getFunction()));
		VK10.vkCmdSetStencilCompareMask(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, stencil.getMask());
		VK10.vkCmdSetStencilWriteMask(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, 0xFF);
		VK10.vkCmdSetStencilReference(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, stencil.getReference());
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
			images[index] = binding.getTexture() instanceof VulkanBorrowedTexture ? ((VulkanBorrowedTexture) binding.getTexture()).getView() : ((VulkanTexture) binding.getTexture()).getView();
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