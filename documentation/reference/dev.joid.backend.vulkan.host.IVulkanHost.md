---
summary: What an application or an engine that renders with Vulkan lends JOID: the device, the queue, the command buffer of the frame and the target image.
---

JOID records into the frame of the host instead of owning one; [[WindowVulkanHost]] and [[SnapshotVulkanHost]] are complete hosts.

Learn: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## getQueueFamilyIndex()

Returns the family of the graphics queue.

## getQueue()

Returns the graphics queue, on which JOID submits the uploads that it does outside a frame.

## getDevice()

Returns the logical device, created with `VK_API_VERSION_1_3`.

## getPhysicalDevice()

Returns the physical device of the logical device.

## getFrameIndex()

Returns the slot of the current frame, from `0` to `getFramesInFlight() - 1`.

## getFrameNumber()

Returns a counter that grows by one at each frame of the host, never per JOID frame.

## getFramesInFlight()

Returns the number of frames in flight, `1` or more.

## getCommandBuffer()

Returns the command buffer of the current frame, recording and outside any render pass.

## getColorFormat()

Returns the format of the target image.

## getTargetView()

Returns the image view that JOID draws into, in `COLOR_ATTACHMENT_OPTIMAL` and already cleared.

## getTargetWidth()

Returns the width of the target image, in pixels.

## getTargetHeight()

Returns the height of the target image, in pixels.
