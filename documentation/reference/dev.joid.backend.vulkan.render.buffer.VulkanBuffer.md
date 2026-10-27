---
summary: A Vulkan buffer in host-visible, coherent memory, mapped for its whole life.
---

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## VulkanBuffer(VulkanContext,long,int)

Creates and maps a buffer of that size in bytes, for that usage.

## destroy()

Unmaps the memory and destroys the buffer and its memory.

## getSize()

Returns the size of the buffer, in bytes.

## getBuffer()

Returns the Vulkan buffer handle.

## getMemory()

Returns the Vulkan memory handle.

## getAddress()

Returns the address of the mapped memory.

## getContext()

Returns the device context.
