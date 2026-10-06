package dev.joid.impl.vulkan.render;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import lombok.Getter;

@Getter
public final class Swapchain {

	private final int     format;
	private final long    window;
	private final int     colorSpace;
	private final Context context;
	private final long    loadRenderPass;
	private final long    clearRenderPass;

	private int    width;
	private int    height;
	private long[] views;
	private long[] images;
	private long   swapchain;
	private long   depthView;
	private long   depthImage;
	private long   depthMemory;
	private long[] framebuffers;

	public Swapchain(final Context context, final long window) {
		this.context = context;
		this.window  = window;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer count = stack.mallocInt(1);
			KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(context.getPhysicalDevice(), context.getSurface(), count, null);
			final VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
			KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(context.getPhysicalDevice(), context.getSurface(), count, formats);

			VkSurfaceFormatKHR selected = formats.get(0);
			for (final VkSurfaceFormatKHR format : formats) {
				if (format.format() == VK10.VK_FORMAT_B8G8R8A8_UNORM && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
					selected = format;
				}
			}

			this.format     = selected.format();
			this.colorSpace = selected.colorSpace();
		}

		this.clearRenderPass = context.createRenderPass(this.format, true, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, true, true);
		this.loadRenderPass  = context.createRenderPass(this.format, false, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, true, false);
		this.create();
	}

	public boolean isOutdated() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		return width[0] != this.width || height[0] != this.height;
	}

	public void recreate() {
		VK10.vkDeviceWaitIdle(this.context.getDevice());
		this.destroyResources();
		this.create();
	}

	private void create() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
			Context.check(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.context.getPhysicalDevice(), this.context.getSurface(), capabilities), "vkGetPhysicalDeviceSurfaceCapabilitiesKHR");

			final int[] framebufferWidth = new int[1];
			final int[] framebufferHeight = new int[1];
			GLFW.glfwGetFramebufferSize(this.window, framebufferWidth, framebufferHeight);
			this.width = capabilities.currentExtent().width() != -1 ? capabilities.currentExtent().width() : Math.max(capabilities.minImageExtent().width(), Math.min(capabilities.maxImageExtent().width(), framebufferWidth[0]));
			this.height = capabilities.currentExtent().height() != -1 ? capabilities.currentExtent().height() : Math.max(capabilities.minImageExtent().height(), Math.min(capabilities.maxImageExtent().height(), framebufferHeight[0]));

			int imageCount = capabilities.minImageCount() + 1;
			if (capabilities.maxImageCount() > 0) {
				imageCount = Math.min(imageCount, capabilities.maxImageCount());
			}

			final VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack)
					.sType$Default()
					.surface(this.context.getSurface())
					.minImageCount(imageCount)
					.imageFormat(this.format)
					.imageColorSpace(this.colorSpace)
					.imageArrayLayers(1)
					.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
					.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
					.preTransform(capabilities.currentTransform())
					.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
					.presentMode(this.selectPresentMode(stack))
					.clipped(true)
					.oldSwapchain(this.swapchain);
			info.imageExtent().set(this.width, this.height);

			final LongBuffer swapchain = stack.mallocLong(1);
			Context.check(KHRSwapchain.vkCreateSwapchainKHR(this.context.getDevice(), info, null, swapchain), "vkCreateSwapchainKHR");
			if (this.swapchain != VK10.VK_NULL_HANDLE) {
				KHRSwapchain.vkDestroySwapchainKHR(this.context.getDevice(), this.swapchain, null);
			}
			this.swapchain = swapchain.get(0);

			final IntBuffer count = stack.mallocInt(1);
			KHRSwapchain.vkGetSwapchainImagesKHR(this.context.getDevice(), this.swapchain, count, null);
			final LongBuffer images = stack.mallocLong(count.get(0));
			KHRSwapchain.vkGetSwapchainImagesKHR(this.context.getDevice(), this.swapchain, count, images);

			final long[] depth = this.context.createImage(this.width, this.height, this.context.getDepthStencilFormat(), VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
			this.depthImage  = depth[0];
			this.depthMemory = depth[1];
			this.depthView   = this.context.createImageView(this.depthImage, this.context.getDepthStencilFormat(), VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);

			this.images       = new long[count.get(0)];
			this.views        = new long[count.get(0)];
			this.framebuffers = new long[count.get(0)];
			for (int i = 0; i < this.images.length; i++) {
				this.images[i] = images.get(i);
				this.views[i] = this.context.createImageView(this.images[i], this.format, VK10.VK_IMAGE_ASPECT_COLOR_BIT);

				final LongBuffer framebuffer = stack.mallocLong(1);
				Context.check(VK10.vkCreateFramebuffer(this.context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(this.clearRenderPass).pAttachments(stack.longs(this.views[i], this.depthView)).width(this.width).height(this.height).layers(1), null, framebuffer), "vkCreateFramebuffer");
				this.framebuffers[i] = framebuffer.get(0);
			}
		}
	}

	private int selectPresentMode(final MemoryStack stack) {
		final IntBuffer count = stack.mallocInt(1);
		KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(this.context.getPhysicalDevice(), this.context.getSurface(), count, null);
		final IntBuffer modes = stack.mallocInt(count.get(0));
		KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(this.context.getPhysicalDevice(), this.context.getSurface(), count, modes);

		int selected = KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
		for (int i = 0; i < modes.capacity(); i++) {
			if (modes.get(i) == KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR) {
				return KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR;
			}

			if (modes.get(i) == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
				selected = KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
			}
		}
		return selected;
	}

	private void destroyResources() {
		for (int i = 0; i < this.images.length; i++) {
			VK10.vkDestroyFramebuffer(this.context.getDevice(), this.framebuffers[i], null);
			VK10.vkDestroyImageView(this.context.getDevice(), this.views[i], null);
		}

		VK10.vkDestroyImageView(this.context.getDevice(), this.depthView, null);
		VK10.vkDestroyImage(this.context.getDevice(), this.depthImage, null);
		VK10.vkFreeMemory(this.context.getDevice(), this.depthMemory, null);
	}

}