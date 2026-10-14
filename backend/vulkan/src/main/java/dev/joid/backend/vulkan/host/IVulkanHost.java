package dev.joid.backend.vulkan.host;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

import lombok.NonNull;

public interface IVulkanHost {

	public int getQueueFamilyIndex();
	public @NonNull VkQueue getQueue();
	public @NonNull VkDevice getDevice();
	public @NonNull VkInstance getInstance();
	public @NonNull VkPhysicalDevice getPhysicalDevice();

	public int getFrameIndex();
	public int getFramesInFlight();
	public @NonNull VkCommandBuffer getCommandBuffer();

	public int getColorFormat();
	public long getTargetView();
	public long getTargetImage();

	public int getTargetWidth();
	public int getTargetHeight();

}