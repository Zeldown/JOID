---
summary: Hands out aligned ranges of one buffer for the vertices or the uniforms of a frame, reset when the slot of the frame comes back.
---

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## VulkanFrameAllocator(VulkanContext,long,int,long,Consumer)

Creates the allocator with a buffer of that capacity in bytes, that usage and that alignment; the consumer defers the destruction of a buffer that grew.

## destroy()

Destroys the current buffer.

## allocate(long)

Returns the offset of an aligned range of that size in the current buffer.
When the buffer is full, it replaces it with a buffer twice as large, defers the destruction of the old one, and returns `0`.

## reset()

Makes the whole buffer free again, at the start of a frame of its slot.

## getUsage()

Returns the usage of the buffers.

## getAlignment()

Returns the alignment of the offsets, in bytes.

## getContext()

Returns the device context.

## getDisposer()

Returns the consumer that defers the destruction of a replaced buffer.

## getOffset()

Returns the first free byte of the current buffer.

## getBuffer()

Returns the current buffer.
