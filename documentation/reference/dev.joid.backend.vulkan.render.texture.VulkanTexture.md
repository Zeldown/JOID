---
summary: A texture of Vulkan in device memory, uploaded through a staging buffer, with mipmaps built by linear blits.
---

A texture larger than the maximum size of the device throws an `IllegalArgumentException` that starts with `A texture of`.

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## create(VulkanRenderBridge)

Creates the texture, without storage; [[VulkanRenderBridge#createTexture]] calls it.

## getBridge()

Returns the render bridge that created it.

## getView()

Returns the image view, `VK_NULL_HANDLE` before the storage exists.

## getImage()

Returns the image handle.

## getMemory()

Returns the memory handle.
