---
summary: The backend for Vulkan, which records the frames of JOID into the frame of a Vulkan host and reads a GLFW window.
---

The host owns the device, the command buffers and the image that JOID draws into: [[WindowVulkanHost]] for a window of your own, or your engine through [[IVulkanHost]].
The loop draws each JOID frame between `beginFrame()` and `endFrame()` of the host.

Learn: [Start the other backends](/use/learn/running/backends/#start-the-other-backends), [Vulkan backend](/build/backends/vulkan/).

## create(long,IVulkanHost)

Creates the backend for the GLFW window of that handle, created without client API, and the Vulkan host that JOID draws into.

## destroy()

Waits for the device, then releases every resource of the renderer; call it after the loop, before you destroy the host.
Throws an `IllegalStateException` before `register()` or on a second call.
