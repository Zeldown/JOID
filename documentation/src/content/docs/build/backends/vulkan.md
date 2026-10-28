---
title: "Vulkan backend"
description: "What the Vulkan backend is made of: its own renderer on Vulkan 1.3, the host that owns the device and the frame, and the contract of a Vulkan host of your own."
genre: build
sidebar:
  order: 3
---

`joid-backend-vulkan` runs JOID on Vulkan 1.3 through LWJGL 3, with a GLFW window.
It is the model for a backend with a renderer of its own: everything above the render bridge is shared with LWJGL 3.
How an application installs and starts it is on [Choosing a backend](/use/learn/running/backends/).

## What it is made of

| Bridge | Class | From |
|---|---|---|
| Render | [[VulkanRenderBridge]] | this backend |
| Audio | [[AlAudioBridge]] over [[Lwjgl3AlBinding]] | [base-openal](/build/base-modules/openal/), [base-glfw](/build/base-modules/glfw/) |
| Window | [[GlfwWindowBridge]] | base-glfw |
| Input | [[GlfwInputForwarder]] | base-glfw |

[[VulkanBackend]] registers them from a GLFW window and an [[IVulkanHost]]: the host owns the instance, the device, the queue, the command buffers and the image that JOID draws into.
For a window of its own, an application uses [[WindowVulkanHost]], which also owns the surface and the swapchain:

| Class | Role |
|---|---|
| [[WindowVulkanHost]] | The host of a GLFW window: device, swapchain recreated with the size of the window, 2 frames in flight by default, present in `MAILBOX` when the surface offers it, `FIFO` otherwise. |
| [[VulkanRenderBridge]] | Records each JOID frame into the command buffer of the host. |
| [[VulkanContext]] | The device of the host, its render passes, and one-shot submissions for the work done outside a frame. |
| [[VulkanPipelineCache]] | One pipeline per [[PipelineKey]] and kind of target; the viewport, the scissor, the cull mode, the front face, the depth test and write and the stencil are dynamic states. |
| [[VulkanDescriptorCache]] | Descriptor sets per shader, uniform buffer and images. |
| [[VulkanFrameAllocator]], [[VulkanBuffer]] | Vertex and uniform memory, one allocator per frame in flight, reset when a new frame of the host starts. |
| [[VulkanShaderTranslator]], [[VulkanShaderCompiler]], [[VulkanShader]] | GLSL 4.50 with the `binding` and `location` qualifiers of SPIR-V, compiled by shaderc at run time. |
| [[VulkanTexture]], [[VulkanFrameBuffer]], [[VulkanImage]] | Images with staging uploads and linear mip blits, and offscreen targets with a color and a depth image. |

## What differs

### Render passes start lazily

A render pass starts at the first clear or draw of a target and ends when the target changes or the frame ends.
The first pass of the frame on the screen clears the depth and the stencil and keeps the color, which the host cleared; later passes keep all three.

### The conventions of OpenGL

The renderer reproduces the OpenGL conventions of the [render contract](/build/bridges/render/#guarantees):

- on the screen, the viewport is flipped; on a framebuffer, nothing is flipped, so the rows land bottom-up as on OpenGL, and the front faces are the clockwise ones to keep the culling of OpenGL;
- the projection goes through [[DepthRange#toZeroToOne]] before it reaches the shaders;
- missing vertex attributes are filled by [[VertexFill#complete]];
- the stencil test applies to the screen only, and offscreen targets have no stencil ([Framebuffers and depth](/build/framebuffers-and-depth/)).

### Destruction waits for the GPU

A texture, a framebuffer or a buffer deleted during a frame is destroyed once the frames in flight that may still read it are over, so a node can recreate its framebuffer in the middle of its own pass.
A shader that does not compile prints one warning and is not drawn, as on OpenGL.

### Requirements

The device must support Vulkan 1.3: [[VulkanRenderBridge]] throws an `IllegalStateException` that asks for `VK_API_VERSION_1_3` otherwise.
The window, the stack size and the loop that the backend needs are on [Start the other backends](/use/learn/running/backends/#start-the-other-backends).

### Drawings of the host

`suspend` ends the current render pass and runs the drawing, which can record its own commands into the command buffer of the host.
`raster` begins a pass on the JOID framebuffer, with the viewport and the scissor of its pixel size, and runs the drawing inside it.

## Run inside a Vulkan host

An engine that already renders with Vulkan implements [[IVulkanHost]] and passes it to `VulkanBackend.create(window, host)`: JOID then records into its frame instead of owning one.
[[SnapshotVulkanHost]], the host of the tests, is a complete host without a surface: one offscreen image, one frame in flight.

| Member | What it returns |
|---|---|
| [[IVulkanHost#getDevice]], [[IVulkanHost#getPhysicalDevice]] | The device and its physical device. |
| [[IVulkanHost#getQueue]], [[IVulkanHost#getQueueFamilyIndex]] | The graphics queue and its family, where JOID submits the work it does outside a frame. |
| [[IVulkanHost#getCommandBuffer]] | The command buffer of the current frame, recording. |
| [[IVulkanHost#getFramesInFlight]], [[IVulkanHost#getFrameIndex]] | The number of frames in flight, 1 or more, and the slot of the current frame, from `0` to that number minus one. |
| [[IVulkanHost#getFrameNumber]] | A counter that grows by one at each frame of the host. |
| [[IVulkanHost#getTargetView]], [[IVulkanHost#getColorFormat]] | The image view that JOID draws into, and its format. |
| [[IVulkanHost#getTargetWidth]], [[IVulkanHost#getTargetHeight]] | The size of that image, in pixels. |

| Guarantee | Why |
|---|---|
| Create the instance and the device with `VK_API_VERSION_1_3`. | The renderer uses the dynamic states of Vulkan 1.3. |
| Keep the command buffer recording and outside any render pass while `frame()` runs. | JOID begins and ends its own render passes in it. |
| Put the target image in `COLOR_ATTACHMENT_OPTIMAL` before `frame()`, and clear it yourself. | JOID loads the color of the target and leaves it in that layout. |
| Increment the frame number once per frame of the host, never per JOID frame. | JOID resets its allocators and releases its deferred resources only when the number changes, so several JOID frames can share one frame of the host. |
| Wait for the fence of a slot before you record into it again. | JOID reuses the memory of a slot once `getFramesInFlight()` frames have passed. |
| Submit your frame before you call JOID outside a frame, when you run several JOID frames per frame of the host. | Outside a frame, JOID submits its uploads (textures, mipmaps, framebuffers) at once on the queue, ahead of the commands already recorded in your buffer. |
| Call [[VulkanRenderBridge#releaseHandle]] with an image view that a [[VulkanBorrowedTexture]] lent, before you destroy that view. | JOID caches descriptor sets per view, and a new view can reuse the value of a destroyed one. |

## Tests

[[VulkanSnapshotBackend]] opens a hidden GLFW window without client API for the window and input bridges, and renders through a [[SnapshotVulkanHost]] into an offscreen `B8G8R8A8_UNORM` image, whatever the screens of the machine.
[Testing a backend](/build/testing-a-backend/#the-suites) runs the contract suites and the snapshot suite on your backend; the state guard suite has no trap to set on Vulkan, which has no global state shared with a host.
