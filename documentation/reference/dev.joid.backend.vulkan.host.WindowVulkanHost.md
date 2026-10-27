---
summary: The Vulkan host of a GLFW window, which owns the instance, the device and a swapchain that follows the size of the window.
---

It presents in `MAILBOX` when the surface offers it, `FIFO` otherwise.

```java fragment
final WindowVulkanHost host = WindowVulkanHost.create(window);
final VulkanBackend backend = VulkanBackend.create(window, host).register();
```

Learn: [Start the other backends](/use/learn/running/backends/#start-the-other-backends), [Vulkan backend](/build/backends/vulkan/).

## create(long)

Creates the host of the window with 2 frames in flight.
Throws an `IllegalStateException` when Vulkan is missing, or when no Vulkan 1.3 device can present to the window.

## create(long,int)

Creates the host of the window with that many frames in flight: `1` waits for each frame, `2` or more records the next frame while the GPU draws the previous ones.
Throws an `IllegalArgumentException` below `1`.

## destroy()

Waits for the device and destroys the swapchain, the surface, the device and the instance; call it after `VulkanBackend.destroy()`.

## beginFrame()

Waits for the slot of the frame, acquires the next image of the swapchain, recreating it when the window changed size, starts the command buffer and clears the image to black.
Throws an `IllegalStateException` when a frame is already begun.

## endFrame()

Ends the command buffer, submits it and presents the image.
Throws an `IllegalStateException` when no frame is begun.

## getCommandBuffer()

Returns the command buffer of the current slot.

## getTargetView()

Returns the view of the acquired image of the swapchain.

## getTargetImage()

Returns the acquired image of the swapchain.

## getQueue()

Returns the graphics and present queue.

## getDevice()

Returns the logical device.

## getColorFormat()

Returns the format of the swapchain images.

## getFramesInFlight()

Returns the number of frames in flight.

## getInstance()

Returns the Vulkan instance.

## getQueueFamilyIndex()

Returns the family of the queue.

## getPhysicalDevice()

Returns the physical device.

## getFrameIndex()

Returns the slot of the current frame.

## getTargetWidth()

Returns the width of the swapchain images, in pixels.

## getTargetHeight()

Returns the height of the swapchain images, in pixels.

## getFrameNumber()

Returns the number of frames begun since the host was created.

## isFrameActive()

If `true`, a frame is begun and not ended.
