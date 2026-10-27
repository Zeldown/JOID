---
summary: The graphics pipelines of the Vulkan renderer, one per pipeline key and kind of target.
---

The viewport, the scissor, the cull mode, the front face, the depth test and write and the stencil are dynamic states, so they create no pipeline.

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## VulkanPipelineCache(VulkanContext)

Creates an empty cache over the device.

## destroy()

Destroys every pipeline.

## get(PipelineKey,boolean,long)

Returns the pipeline of that key for the screen or an offscreen target, created at its first use with that render pass.

## invalidate(VulkanShader,Consumer)

Forgets every pipeline of the shader and destroys them through the consumer, once no frame uses them.
