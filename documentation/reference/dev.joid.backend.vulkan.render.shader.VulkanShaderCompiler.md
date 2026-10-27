---
summary: Compiles a translated GLSL stage to SPIR-V with shaderc, at run time.
---

Learn: [What it is made of](/build/backends/vulkan/#what-it-is-made-of).

## compile(String,ShaderStage)

Returns the SPIR-V code of the source, in a direct buffer, or `null` after printing the error of shaderc when the source does not compile.
