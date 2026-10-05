package be.zeldown.joid.impl.vulkan.snapshot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import be.zeldown.joid.impl.vulkan.Backend;
import be.zeldown.joid.impl.vulkan.render.Context;
import be.zeldown.joid.impl.vulkan.render.RenderBridge;
import be.zeldown.joid.impl.vulkan.render.Swapchain;
import be.zeldown.joid.impl.vulkan.render.buffer.Buffer;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.test.snapshot.ISnapshotBackend;
import be.zeldown.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class SnapshotBackend implements ISnapshotBackend {

	private long window;

	@Override
	public void destroy() {
		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
	}

	@Override
	public void present() {
		((RenderBridge) BridgeHandler.RENDER.get()).present();
	}

	@Override
	public @NonNull String getRenderer() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
			VK10.vkGetPhysicalDeviceProperties(((RenderBridge) BridgeHandler.RENDER.get()).getContext().getPhysicalDevice(), properties);
			return properties.deviceNameString();
		}
	}

	@Override
	public void frame(final @NonNull Runnable draw) {
		final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
		render.beginFrame();
		draw.run();
		render.endFrame();
	}

	@Override
	public void create(final int width, final int height) {
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

		this.window = GLFW.glfwCreateWindow(width, height, "JOID snapshot", 0L, 0L);
		if (this.window == 0L) {
			throw new IllegalStateException("Unable to create the GLFW window");
		}

		Backend.register(this.window);
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
		final SnapshotImage snapshot = SnapshotImage.fromBytes(MemoryUtil.memByteBuffer(buffer.getAddress(), width * height * 4), width, height, false, bgra);
		buffer.destroy();
		return snapshot;
	}

}