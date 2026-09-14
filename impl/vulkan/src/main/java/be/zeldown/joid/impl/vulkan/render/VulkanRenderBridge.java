package be.zeldown.joid.impl.vulkan.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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

import be.zeldown.joid.impl.vulkan.render.buffer.VulkanBuffer;
import be.zeldown.joid.impl.vulkan.render.buffer.VulkanStream;
import be.zeldown.joid.impl.vulkan.render.descriptor.VulkanDescriptorCache;
import be.zeldown.joid.impl.vulkan.render.framebuffer.VulkanFrameBuffer;
import be.zeldown.joid.impl.vulkan.render.pipeline.VulkanPipelineCache;
import be.zeldown.joid.impl.vulkan.render.pipeline.VulkanPipelineKey;
import be.zeldown.joid.impl.vulkan.render.shader.VulkanShader;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.VulkanSamplerUniform;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.VulkanUniformBlock;
import be.zeldown.joid.impl.vulkan.render.shader.uniform.VulkanUniformMember;
import be.zeldown.joid.impl.vulkan.render.texture.VulkanTexture;
import be.zeldown.joid.lib.bridge.render.RenderBridge;
import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.state.RenderState;
import be.zeldown.joid.lib.bridge.render.state.StencilFunction;
import be.zeldown.joid.lib.bridge.render.state.StencilOperation;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanRenderBridge extends RenderBridge {

	private static final long VERTEX_CAPACITY  = 8L << 20;
	private static final long UNIFORM_CAPACITY = 8L << 20;
	private static final long STAGING_CAPACITY = 4L << 20;

	private final VulkanContext         context;
	private final VulkanSwapchain       swapchain;
	private final VulkanPipelineCache   pipelineCache;
	private final VulkanDescriptorCache descriptorCache;
	private final List<Runnable>        garbage;
	private final VulkanStream          vertexStream;
	private final VulkanStream          uniformStream;
	private final VkCommandBuffer       commandBuffer;
	private final long                  imageSemaphore;
	private final long                  frameFence;
	private final long[]                samplers;
	private final VulkanTexture         emptyTexture;
	private final VulkanShader          fixedShader;

	private VulkanBuffer      stagingBuffer;
	private int               imageIndex;
	private boolean           frameActive;
	private boolean           screenCleared;
	private boolean           passActive;
	private VulkanFrameBuffer passTarget;
	private int               passWidth;
	private int               passHeight;
	private long              boundPipeline;

	public VulkanRenderBridge(final long window) {
		this.context         = new VulkanContext(window);
		this.swapchain       = new VulkanSwapchain(this.context, window);
		this.pipelineCache   = new VulkanPipelineCache(this.context);
		this.descriptorCache = new VulkanDescriptorCache(this.context);
		this.garbage         = new ArrayList<>();
		this.vertexStream    = new VulkanStream(this.context, VulkanRenderBridge.VERTEX_CAPACITY, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 1L, this::dispose);
		this.uniformStream   = new VulkanStream(this.context, VulkanRenderBridge.UNIFORM_CAPACITY, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, this.context.getUniformAlignment(), this::dispose);
		this.commandBuffer   = this.context.allocateCommandBuffer();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer semaphore = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateSemaphore(this.context.getDevice(), VkSemaphoreCreateInfo.calloc(stack).sType$Default(), null, semaphore), "vkCreateSemaphore");
			this.imageSemaphore = semaphore.get(0);

			final LongBuffer fence = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateFence(this.context.getDevice(), VkFenceCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT), null, fence), "vkCreateFence");
			this.frameFence = fence.get(0);
		}

		this.samplers     = this.createSamplers();
		this.emptyTexture = new VulkanTexture(this).allocate(1, 1).upload(new int[] {0xFFFFFFFF}, 1, 1);
		this.fixedShader  = VulkanShader.create(this, VulkanRenderBridge.read(VulkanRenderBridge.class.getResourceAsStream("/assets/shaders/fixed/fixed.vsh")), VulkanRenderBridge.read(VulkanRenderBridge.class.getResourceAsStream("/assets/shaders/fixed/fixed.fsh")), BlendState.DISABLED);
	}

	public void beginFrame() {
		if (this.frameActive) {
			throw new IllegalStateException("The Vulkan frame has already begun");
		}

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

		this.vertexStream.reset();
		this.uniformStream.reset();
		this.frameActive   = true;
		this.screenCleared = false;
		this.passActive    = false;
		this.passTarget    = null;
		this.boundPipeline = VK10.VK_NULL_HANDLE;
	}

	public void endFrame() {
		this.requireFrame();
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

		this.frameActive = false;
		this.garbage.forEach(Runnable::run);
		this.garbage.clear();
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
	public void clear(final float red, final float green, final float blue, final float alpha) {
		this.requireFrame();
		this.beginPass((VulkanFrameBuffer) super.getState().getFrameBuffer());
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).colorAttachment(0);
			attachment.clearValue().color().float32(0, red).float32(1, green).float32(2, blue).float32(3, alpha);
			VK10.vkCmdClearAttachments(this.commandBuffer, attachment, this.createClearRect(stack));
		}
	}

	@Override
	public void clearStencil() {
		this.requireFrame();
		if (super.getState().getFrameBuffer() != null) {
			return;
		}

		this.beginPass(null);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
			attachment.clearValue().depthStencil().stencil(0);
			VK10.vkCmdClearAttachments(this.commandBuffer, attachment, this.createClearRect(stack));
		}
	}

	@Override
	public void draw(final @NonNull DrawMode mode, final @NonNull VertexBuffer buffer) {
		this.requireFrame();
		final RenderState state = super.getState();
		final VulkanShader shader = state.getShader() == null ? this.fixedShader : (VulkanShader) state.getShader();
		final int topology = VulkanRenderBridge.topology(mode);
		if (!shader.isActive() || buffer.getCount() == 0 || state.getViewportWidth() <= 0 || state.getViewportHeight() <= 0) {
			return;
		}

		final VulkanFrameBuffer target = (VulkanFrameBuffer) state.getFrameBuffer();
		this.beginPass(target);

		final boolean lines = topology == VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST || topology == VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
		final long pipeline = this.pipelineCache.get(new VulkanPipelineKey(shader, target != null, state.getBlend(), state.isColorMask(), lines, lines && state.isLineSmooth()), target == null ? this.swapchain.getClearRenderPass() : this.context.getOffscreenRenderPass());
		if (pipeline != this.boundPipeline) {
			VK10.vkCmdBindPipeline(this.commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
			this.boundPipeline = pipeline;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			this.applyDynamicState(stack, state, target != null, topology);
			final long vertexOffset = this.writeVertices(state, buffer);
			final IntBuffer dynamicOffsets = this.writeUniforms(stack, state, shader);
			final long descriptorSet = this.descriptorCache.get(shader, this.uniformStream.getBuffer().getBuffer(), this.getImages(state, shader));

			VK10.vkCmdBindDescriptorSets(this.commandBuffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, shader.getPipelineLayout(), 0, stack.longs(descriptorSet), dynamicOffsets);
			VK10.vkCmdBindVertexBuffers(this.commandBuffer, 0, stack.longs(this.vertexStream.getBuffer().getBuffer()), stack.longs(vertexOffset));
			VK10.vkCmdDraw(this.commandBuffer, buffer.getCount(), 1, 0, 0);
		}
	}

	@Override
	public @NonNull ITexture createTexture() {
		return new VulkanTexture(this);
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height, final @NonNull TextureFilter filter) {
		return VulkanFrameBuffer.create(this, width, height);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull InputStream vertex, final @NonNull InputStream fragment, final @NonNull BlendState blend) {
		return VulkanShader.create(this, VulkanRenderBridge.read(vertex), VulkanRenderBridge.read(fragment), blend);
	}

	public void dispose(final @NonNull Runnable destroyer) {
		if (this.frameActive) {
			this.garbage.add(destroyer);
		} else {
			destroyer.run();
		}
	}

	public void releaseHandle(final long handle) {
		this.descriptorCache.invalidate(handle, this::dispose);
	}

	public @NonNull VulkanBuffer getStagingBuffer(final long size) {
		if (this.stagingBuffer == null || this.stagingBuffer.getSize() < size) {
			if (this.stagingBuffer != null) {
				this.stagingBuffer.destroy();
			}

			this.stagingBuffer = VulkanBuffer.create(this.context, Math.max(size, VulkanRenderBridge.STAGING_CAPACITY), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		}
		return this.stagingBuffer;
	}

	public long getSampler(final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		return this.samplers[filter.ordinal() * TextureWrap.values().length + wrap.ordinal()];
	}

	private void requireFrame() {
		if (!this.frameActive) {
			throw new IllegalStateException("Vulkan rendering must happen between beginFrame and endFrame");
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
				info.renderPass(this.context.getOffscreenRenderPass()).framebuffer(target.getFramebuffer());
			}

			info.renderArea().extent().set(this.passWidth, this.passHeight);
			VK10.vkCmdBeginRenderPass(this.commandBuffer, info, VK10.VK_SUBPASS_CONTENTS_INLINE);
		}

		this.passActive    = true;
		this.passTarget    = target;
		this.boundPipeline = VK10.VK_NULL_HANDLE;
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

		VK10.vkCmdSetLineWidth(this.commandBuffer, this.context.isWideLines() ? state.getLineWidth() : 1F);
		VK13.vkCmdSetPrimitiveTopology(this.commandBuffer, topology);
		VK13.vkCmdSetCullMode(this.commandBuffer, state.isCull() ? VK10.VK_CULL_MODE_BACK_BIT : VK10.VK_CULL_MODE_NONE);
		VK13.vkCmdSetFrontFace(this.commandBuffer, offscreen ? VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE : VK10.VK_FRONT_FACE_CLOCKWISE);
		VK13.vkCmdSetDepthTestEnable(this.commandBuffer, state.isDepthTest());
		VK13.vkCmdSetDepthWriteEnable(this.commandBuffer, state.isDepthWrite());
		VK13.vkCmdSetDepthCompareOp(this.commandBuffer, VK10.VK_COMPARE_OP_LESS);
		VK13.vkCmdSetStencilTestEnable(this.commandBuffer, state.isStencilTest());
		VK13.vkCmdSetStencilOp(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, VulkanRenderBridge.operation(state.getStencilFail()), VulkanRenderBridge.operation(state.getStencilPass()), VulkanRenderBridge.operation(state.getStencilDepthFail()), VulkanRenderBridge.compare(state.getStencilFunction()));
		VK10.vkCmdSetStencilCompareMask(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, state.getStencilMask());
		VK10.vkCmdSetStencilWriteMask(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, 0xFF);
		VK10.vkCmdSetStencilReference(this.commandBuffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, state.getStencilReference());
	}

	private long writeVertices(final RenderState state, final VertexBuffer buffer) {
		final int size = buffer.getCount() * VertexBuffer.STRIDE;
		final long offset = this.vertexStream.allocate(size);
		final long address = this.vertexStream.getBuffer().getAddress() + offset;
		MemoryUtil.memCopy(MemoryUtil.memAddress(buffer.getBuffer()), address, size);
		if (buffer.isTexture() && buffer.isColor() && buffer.isNormal()) {
			return offset;
		}

		final byte red = VulkanRenderBridge.toByte(state.getRed());
		final byte green = VulkanRenderBridge.toByte(state.getGreen());
		final byte blue = VulkanRenderBridge.toByte(state.getBlue());
		final byte alpha = VulkanRenderBridge.toByte(state.getAlpha());
		for (int i = 0; i < buffer.getCount(); i++) {
			final long vertex = address + (long) i * VertexBuffer.STRIDE;
			if (!buffer.isTexture()) {
				MemoryUtil.memPutFloat(vertex + VertexBuffer.TEXTURE_OFFSET, 0F);
				MemoryUtil.memPutFloat(vertex + VertexBuffer.TEXTURE_OFFSET + 4, 0F);
			}

			if (!buffer.isColor()) {
				MemoryUtil.memPutByte(vertex + VertexBuffer.COLOR_OFFSET, red);
				MemoryUtil.memPutByte(vertex + VertexBuffer.COLOR_OFFSET + 1, green);
				MemoryUtil.memPutByte(vertex + VertexBuffer.COLOR_OFFSET + 2, blue);
				MemoryUtil.memPutByte(vertex + VertexBuffer.COLOR_OFFSET + 3, alpha);
			}

			if (!buffer.isNormal()) {
				MemoryUtil.memPutInt(vertex + VertexBuffer.NORMAL_OFFSET, 127 << 16);
			}
		}
		return offset;
	}

	private IntBuffer writeUniforms(final MemoryStack stack, final RenderState state, final VulkanShader shader) {
		final VulkanUniformMember projection = shader.getMemberMap().get("uProjectionMatrix");
		if (projection != null) {
			final float[] matrix = super.getProjection().getMatrix().clone();
			for (int column = 0; column < 4; column++) {
				matrix[column * 4 + 2] = 0.5F * matrix[column * 4 + 2] + 0.5F * matrix[column * 4 + 3];
			}
			projection.putMatrix(matrix);
		}

		final VulkanUniformMember modelView = shader.getMemberMap().get("uModelViewMatrix");
		if (modelView != null) {
			modelView.putMatrix(super.getModelView().getMatrix());
		}

		final VulkanUniformMember normal = shader.getMemberMap().get("uNormalMatrix");
		if (normal != null) {
			normal.putMatrix(super.getModelView().getNormalMatrix());
		}

		final VulkanUniformMember lighting = shader.getMemberMap().get("uLighting");
		if (lighting != null) {
			lighting.putInt(state.isLighting() ? 1 : 0);
		}

		final VulkanUniformMember alphaTest = shader.getMemberMap().get("joid_AlphaTest");
		final VulkanUniformMember alphaThreshold = shader.getMemberMap().get("joid_AlphaThreshold");
		if (alphaTest != null && alphaThreshold != null) {
			alphaTest.putInt(state.isAlphaTest() ? 1 : 0);
			alphaThreshold.putFloats(state.getAlphaThreshold());
		}

		final long previousBuffer = this.uniformStream.getBuffer().getBuffer();
		final IntBuffer offsets = this.uploadBlocks(stack, shader);
		if (this.uniformStream.getBuffer().getBuffer() == previousBuffer) {
			return offsets;
		}

		this.descriptorCache.invalidate(previousBuffer, this::dispose);
		return this.uploadBlocks(stack, shader);
	}

	private IntBuffer uploadBlocks(final MemoryStack stack, final VulkanShader shader) {
		final IntBuffer offsets = stack.mallocInt(shader.getBlocks().size());
		for (final VulkanUniformBlock block : shader.getBlocks()) {
			final ByteBuffer data = block.getData();
			final long offset = this.uniformStream.allocate(data.capacity());
			MemoryUtil.memCopy(MemoryUtil.memAddress(data), this.uniformStream.getBuffer().getAddress() + offset, data.capacity());
			offsets.put((int) offset);
		}
		offsets.flip();
		return offsets;
	}

	private long[] getImages(final RenderState state, final VulkanShader shader) {
		final long[] images = new long[shader.getSamplerBindings().size() * 2];
		int index = 0;
		for (final String name : shader.getSamplerBindings().keySet()) {
			final VulkanSamplerUniform sampler = shader.getSamplerMap().get(name);
			final VulkanTexture stateTexture = (VulkanTexture) state.getTexture();
			if (sampler != null && sampler.getTexture() != null && sampler.getTexture().getView() != VK10.VK_NULL_HANDLE) {
				images[index] = sampler.getTexture().getView();
				images[index + 1] = this.getSampler(sampler.getFilter(), sampler.getWrap());
			} else if (stateTexture != null && stateTexture.getView() != VK10.VK_NULL_HANDLE) {
				images[index] = stateTexture.getView();
				images[index + 1] = this.getSampler(state.getTextureFilter(), state.getTextureWrap());
			} else {
				images[index] = this.emptyTexture.getView();
				images[index + 1] = this.getSampler(TextureFilter.NEAREST, TextureWrap.REPEAT);
			}
			index += 2;
		}
		return images;
	}

	private long[] createSamplers() {
		final long[] samplers = new long[TextureFilter.values().length * TextureWrap.values().length];
		try (MemoryStack stack = MemoryStack.stackPush()) {
			for (final TextureFilter filter : TextureFilter.values()) {
				for (final TextureWrap wrap : TextureWrap.values()) {
					final int filterMode = filter == TextureFilter.LINEAR ? VK10.VK_FILTER_LINEAR : VK10.VK_FILTER_NEAREST;
					final int addressMode = VulkanRenderBridge.addressMode(wrap);
					final VkSamplerCreateInfo info = VkSamplerCreateInfo.calloc(stack)
							.sType$Default()
							.magFilter(filterMode)
							.minFilter(filterMode)
							.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
							.addressModeU(addressMode)
							.addressModeV(addressMode)
							.addressModeW(addressMode)
							.borderColor(VK10.VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK);

					final LongBuffer sampler = stack.mallocLong(1);
					VulkanContext.check(VK10.vkCreateSampler(this.context.getDevice(), info, null, sampler), "vkCreateSampler");
					samplers[filter.ordinal() * TextureWrap.values().length + wrap.ordinal()] = sampler.get(0);
				}
			}
		}
		return samplers;
	}

	private static byte toByte(final float value) {
		return (byte) Math.round(Math.max(0F, Math.min(1F, value)) * 255F);
	}

	private static int topology(final DrawMode mode) {
		switch (mode) {
		case LINES:
			return VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
		case LINE_STRIP:
			return VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
		case POLYGON:
			return VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN;
		case TRIANGLES:
			return VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
		default:
			throw new IllegalArgumentException(mode + " is not supported by Vulkan, convert it before drawing");
		}
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

	private static String read(final InputStream stream) {
		try {
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}