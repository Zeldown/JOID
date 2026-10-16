package dev.joid.backend.vulkan.snapshot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import dev.joid.backend.vulkan.VulkanBackend;
import dev.joid.backend.vulkan.host.WindowVulkanHost;
import dev.joid.backend.vulkan.render.VulkanContext;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.backend.vulkan.render.buffer.VulkanBuffer;
import dev.joid.base.glfw.snapshot.GlfwSnapshotWindow;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.image.PixelLayout;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanSnapshotBackend implements ISnapshotBackend {

	private VulkanBackend      backend;
	private WindowVulkanHost   host;
	private GlfwSnapshotWindow window;

	@Override
	public void destroy() {
		if (this.host.isFrameActive()) {
			this.host.endFrame();
		}

		this.backend.destroy();
		this.host.destroy();
		this.window.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.window  = GlfwSnapshotWindow.create(width, height, () -> GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API));
		this.host    = WindowVulkanHost.create(this.window.getWindow());
		this.backend = VulkanBackend.create(this.window.getWindow(), this.host).audio(false).register();
		this.host.beginFrame();
	}

	@Override
	public void present() {
		if (this.host.isFrameActive()) {
			this.host.endFrame();
		}
		this.host.beginFrame();
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		final VulkanContext context = ((VulkanRenderBridge) BridgeHandler.RENDER.get()).getContext();
		final VkCommandBuffer command = this.host.getCommandBuffer();
		final long image = this.host.getTargetImage();
		final int offset = this.host.getTargetHeight() - height;

		final VulkanBuffer buffer = VulkanBuffer.create(context, (long) width * height * 4L, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			VulkanContext.transition(command, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);

			final VkBufferImageCopy.Buffer copy = VkBufferImageCopy.calloc(1, stack);
			copy.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
			copy.imageOffset().set(0, offset, 0);
			copy.imageExtent().set(width, height, 1);
			VK10.vkCmdCopyImageToBuffer(command, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, buffer.getBuffer(), copy);

			VulkanContext.transition(command, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		}

		this.host.endFrame();
		VK10.vkQueueWaitIdle(this.host.getQueue());

		final boolean bgra = this.host.getColorFormat() == VK10.VK_FORMAT_B8G8R8A8_UNORM || this.host.getColorFormat() == VK10.VK_FORMAT_B8G8R8A8_SRGB;
		final SnapshotImage snapshot = SnapshotImage.fromBytes(MemoryUtil.memByteBuffer(buffer.getAddress(), width * height * 4), width, height, false, bgra ? PixelLayout.BGRA8 : PixelLayout.RGBA8);
		buffer.destroy();
		return snapshot;
	}

	@Override
	public @NonNull String getRenderer() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
			VK10.vkGetPhysicalDeviceProperties(this.host.getPhysicalDevice(), properties);
			return properties.deviceNameString();
		}
	}

}