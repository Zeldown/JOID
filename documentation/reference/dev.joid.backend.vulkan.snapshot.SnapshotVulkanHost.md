---
summary: The Vulkan host of the snapshot tests: no surface, one offscreen target image and one frame in flight.
---

It ships in the dev and demo jars of the backend.

Learn: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## create(int,int)

Creates an instance, a device and a `B8G8R8A8_UNORM` target image of that size in pixels.
Throws an `IllegalStateException` when no Vulkan 1.3 device can draw.

## destroy()

Waits for the device and destroys the target, the device and the instance.

## beginFrame()

Waits for the previous frame, starts the command buffer and clears the target image to black.

## endFrame()

Ends the command buffer and submits it; the next `beginFrame()` waits for it.

## getFrameIndex()

Returns `0`.

## getFramesInFlight()

Returns `1`.

## getColorFormat()

Returns `B8G8R8A8_UNORM`.

## getTargetView()

Returns the view of the target image.

## getTargetImage()

Returns the target image.

## getQueue()

Returns the graphics queue.

## getDevice()

Returns the logical device.

## getTargetWidth()

Returns the width of the target image, in pixels.

## getTargetHeight()

Returns the height of the target image, in pixels.

## getQueueFamilyIndex()

Returns the family of the queue.

## getCommandBuffer()

Returns the command buffer of the frame.

## getPhysicalDevice()

Returns the physical device.

## getFrameNumber()

Returns the number of frames begun since the host was created.

## isFrameActive()

If `true`, a frame is begun and not ended.
