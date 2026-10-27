---
summary: A shader of Vulkan: the two stages compiled to SPIR-V, with their descriptor set layout and pipeline layout.
---

A stage that does not compile prints the error of shaderc and leaves the shader inactive, without throwing.

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of), [Shader translation](/build/shader-translation/).

## create(VulkanRenderBridge,ShaderSource,ShaderSource,BlendState)

Creates the shader; [[VulkanRenderBridge#createShader]] calls it.

## getVertexModule()

Returns the shader module of the vertex stage.

## getFragmentModule()

Returns the shader module of the fragment stage.

## getPipelineLayout()

Returns the pipeline layout of the shader.

## isProgramActive()

If `true`, both stages compiled.

## getDescriptorSetLayout()

Returns the descriptor set layout of the uniform block and the samplers.
