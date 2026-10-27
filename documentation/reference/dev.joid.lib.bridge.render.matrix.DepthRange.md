---
summary: Converts a projection from the OpenGL depth range, -1 to 1, to a clip space whose depth goes from 0 to 1.
---

A backend whose API expects a depth from 0 to 1, such as Vulkan, converts the projection with it before it writes the matrix into the uniforms of a shader.
X and Y stay as they are.

Learn: [Depth](/build/framebuffers-and-depth/#depth).

## toZeroToOne(float[])

Returns a converted copy of a column-major 4×4 matrix, 16 floats; the argument stays unchanged.
