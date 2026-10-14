package dev.joid.backend.vulkan.state;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.backend.vulkan.snapshot.VulkanSnapshotBackend;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.test.contract.IStateGuardBackend;
import dev.joid.test.contract.StateTrap;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class VulkanStateGuardBackend implements IStateGuardBackend {

	private final VulkanSnapshotBackend backend = new VulkanSnapshotBackend();

	@Override
	public void destroy() {
		this.backend.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.backend.create(width, height);
	}

	@Override
	public void present() {
		this.backend.present();
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		return this.backend.capture(width, height);
	}

	@Override
	public @NonNull String getRenderer() {
		return this.backend.getRenderer();
	}

	@Override
	public void fill(final int x, final int y, final int width, final int height, final int color) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).colorAttachment(0);
			attachment.clearValue().color().float32(0, (color >> 16 & 255) / 255F).float32(1, (color >> 8 & 255) / 255F).float32(2, (color & 255) / 255F).float32(3, (color >>> 24) / 255F);
			final VkClearRect.Buffer rect = VkClearRect.calloc(1, stack).baseArrayLayer(0).layerCount(1);
			rect.rect().offset().set(x, y);
			rect.rect().extent().set(width, height);
			VK10.vkCmdClearAttachments(this.backend.getHost().getCommandBuffer(), attachment, rect);
		}
	}

	@Override
	public void drawExternal() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkCommandBuffer buffer = this.backend.getHost().getCommandBuffer();
			VK10.vkCmdSetViewport(buffer, 0, VkViewport.calloc(1, stack).x(3F).y(3F).width(10F).height(10F).minDepth(0F).maxDepth(1F));
			final VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.offset().set(2, 2);
			scissor.extent().set(4, 4);
			VK10.vkCmdSetScissor(buffer, 0, scissor);
			VK10.vkCmdSetStencilReference(buffer, VK10.VK_STENCIL_FACE_FRONT_AND_BACK, 5);
		}
	}

	@Override
	public void inject(final @NonNull StateTrap trap) {
		throw new IllegalArgumentException("Vulkan has no global state for the trap " + trap + " to set: JOID records into the command buffer of the host");
	}

	@Override
	public boolean supports(final @NonNull StateTrap trap) {
		return false;
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readState() {
		final Map<String, String> state = new TreeMap<>();
		try {
			final Field passActive = VulkanRenderBridge.class.getDeclaredField("passActive");
			passActive.setAccessible(true);
			state.put("RENDER_PASS", Boolean.toString(passActive.getBoolean(BridgeHandler.RENDER.get())));
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
		return state;
	}

}