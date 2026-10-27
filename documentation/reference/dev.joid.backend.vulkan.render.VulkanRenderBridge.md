---
summary: The render bridge of Vulkan, which records each JOID frame into the command buffer of the host with the conventions of OpenGL.
---

It needs a device with Vulkan 1.3.
A resource deleted during a frame is destroyed once the frames in flight that may still read it are over.

Learn: [Vulkan backend](/build/backends/vulkan/), [Render bridge](/build/bridges/render/).

## VulkanRenderBridge(IVulkanHost)

Creates the renderer for the host.
Throws an `IllegalArgumentException` when the host has no frame in flight, and an `IllegalStateException` when its device lacks Vulkan 1.3.

## destroy()

Waits for the device and releases every resource of the renderer; [[VulkanBackend#destroy]] calls it.

## dispose(Runnable)

Runs the destruction once the frames in flight that may still use the resource are over.

## record(Consumer)

Records commands into the command buffer of the frame, after ending the current render pass; outside a frame, submits them at once and waits.

## createTexture()

Returns an empty [[VulkanTexture]].

## createFrameBuffer(int,int)

Returns a [[VulkanFrameBuffer]] of that size in pixels.

## createShader(ShaderSource,ShaderSource,BlendState)

Returns a [[VulkanShader]], translated to GLSL 4.50 and compiled to SPIR-V.

## suspend(Runnable)

Ends the current render pass and runs the drawing, which can record its own commands into the command buffer of the host.

## raster(IFrameBuffer,int,int,Runnable)

Begins a render pass on the framebuffer with a viewport and a scissor of that size in pixels, and runs the drawing inside it.

## releaseHandle(long)

Forgets the descriptor sets that use that image view; call it before you destroy a view that a [[VulkanBorrowedTexture]] lent.

## getHost()

Returns the Vulkan host.

## getContext()

Returns the device context of the renderer.

## getPipelineCache()

Returns the pipelines of the renderer.

## getDescriptorCache()

Returns the descriptor sets of the renderer.
