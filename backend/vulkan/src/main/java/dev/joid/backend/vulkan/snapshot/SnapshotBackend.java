package dev.joid.backend.vulkan.snapshot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import dev.joid.backend.vulkan.Backend;
import dev.joid.backend.vulkan.render.Context;
import dev.joid.backend.vulkan.render.RenderBridge;
import dev.joid.backend.vulkan.render.Swapchain;
import dev.joid.backend.vulkan.render.buffer.Buffer;
import dev.joid.base.glfw.snapshot.GlfwSnapshotWindow;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.image.PixelLayout;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class SnapshotBackend implements ISnapshotBackend {

	private GlfwSnapshotWindow window;

	@Override
	public void destroy() {
		this.window.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.window = GlfwSnapshotWindow.create(width, height, () -> GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API));
		Backend.register(this.window.getWindow());
	}

	@Override
	public void present() {
		((RenderBridge) BridgeHandler.RENDER.get()).present();
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
		final Context context = render.getContext();
		final Swapchain swapchain = render.getSwapchain();
		final long image = swapchain.getImages()[render.getImageIndex()];
		final int offset = swapchain.getHeight() - height;

		final Buffer buffer = Buffer.create(context, (long) width * height * 4L, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
		context.submit(command -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				Context.transition(command, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);

				final VkBufferImageCopy.Buffer copy = VkBufferImageCopy.calloc(1, stack);
				copy.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				copy.imageOffset().set(0, offset, 0);
				copy.imageExtent().set(width, height, 1);
				VK10.vkCmdCopyImageToBuffer(command, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, buffer.getBuffer(), copy);

				Context.transition(command, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
			}
		});

		final boolean bgra = swapchain.getFormat() == VK10.VK_FORMAT_B8G8R8A8_UNORM || swapchain.getFormat() == VK10.VK_FORMAT_B8G8R8A8_SRGB;
		final SnapshotImage snapshot = SnapshotImage.fromBytes(MemoryUtil.memByteBuffer(buffer.getAddress(), width * height * 4), width, height, false, bgra ? PixelLayout.BGRA8 : PixelLayout.RGBA8);
		buffer.destroy();
		return snapshot;
	}

	@Override
	public @NonNull String getRenderer() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
			VK10.vkGetPhysicalDeviceProperties(((RenderBridge) BridgeHandler.RENDER.get()).getContext().getPhysicalDevice(), properties);
			return properties.deviceNameString();
		}
	}

}