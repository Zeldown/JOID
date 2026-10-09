package dev.joid.backend.vulkan.render.texture;

import java.util.function.Supplier;

@FunctionalInterface
public interface IVulkanImageSupplier extends Supplier<VulkanImage> {}