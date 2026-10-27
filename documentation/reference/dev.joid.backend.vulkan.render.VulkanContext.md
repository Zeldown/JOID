---
summary: The device of the host seen by the Vulkan renderer, with its render passes, image and buffer creation, and one-shot submissions.
---

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## TEXTURE_FORMAT

The format of every texture and framebuffer color image of JOID, `B8G8R8A8_UNORM`.

## VulkanContext(IVulkanHost)

Reads the device of the host, its limits and its depth formats, and creates a command pool and the render pass of the offscreen targets.
Throws an `IllegalStateException` when the device supports no depth format of JOID.

## destroy()

Destroys the render pass of the offscreen targets and the command pool.

## createRenderPass(int,int,boolean)

Returns a render pass that loads the color of that format, and clears or loads the depth of the other format, the stencil with it when the format has one.

## createImage(int,int,int,int,int,int)

Creates an image of device memory and its view, and returns the image, the memory and the view handles, in that order.

## destroyImage(long[])

Destroys the view, the image and the memory of an image created by `createImage`.

## createBuffer(long,int,int)

Creates a buffer with memory of those properties, and returns the buffer and the memory handles.

## submit(Consumer)

Records commands into a one-shot command buffer, submits it and waits for the queue.

## transition(VkCommandBuffer,long,int,int,int)

Records a barrier that moves the first level of the image from one layout to another.

## transition(VkCommandBuffer,long,int,int,int,int,int)

Records a barrier that moves a range of levels of the image from one layout to another.

## createCommandPool(VkDevice,int)

Creates a command pool for that queue family, whose buffers can be reset.

## createImageView(VkDevice,long,int,int,int)

Creates a 2D view of the image, with that format, aspect and number of levels.

## check(int,String)

Throws an `IllegalStateException` that names the call when the Vulkan result is not `VK_SUCCESS`.

## getQueue()

Returns the queue of the host.

## getDevice()

Returns the logical device.

## getDepthFormat()

Returns the depth format of the offscreen targets.

## getCommandPool()

Returns the command pool of the one-shot submissions.

## getDeviceName()

Returns the name of the physical device.

## getMaxTextureSize()

Returns the largest width and height of an image, in pixels.

## getUniformAlignment()

Returns the alignment of the offsets of uniform buffers, in bytes.

## getDepthStencilFormat()

Returns the depth and stencil format of the screen target.

## getOffscreenRenderPass()

Returns the render pass of the offscreen targets.

## getPhysicalDevice()

Returns the physical device.

## getMemoryProperties()

Returns the memory types and heaps of the physical device.
