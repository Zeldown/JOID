---
summary: The descriptor sets of the Vulkan renderer, one per shader, uniform buffer and set of image views.
---

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## VulkanDescriptorCache(VulkanContext)

Creates an empty cache over the device.

## destroy()

Destroys the descriptor pools and every set.

## get(VulkanShader,long,long[])

Returns the descriptor set of the shader for that uniform buffer and those image views, written at its first use.

## invalidate(long,Consumer)

Forgets every set that uses that handle and frees them through the consumer, once no frame uses them.
