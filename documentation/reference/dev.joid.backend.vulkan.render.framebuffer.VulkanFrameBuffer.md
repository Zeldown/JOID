---
summary: An offscreen target of Vulkan with a color texture and a depth image, without stencil.
---

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of), [Framebuffers and depth](/build/framebuffers-and-depth/).

## VulkanFrameBuffer(VulkanRenderBridge,int,int)

Creates the color texture, the depth image and the framebuffer at that size in pixels; [[VulkanRenderBridge#createFrameBuffer]] calls it.

## getFramebuffer()

Returns the Vulkan framebuffer handle.

## getBridge()

Returns the render bridge that created it.
