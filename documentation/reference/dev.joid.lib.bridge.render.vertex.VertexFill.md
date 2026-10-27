---
summary: Copies a vertex buffer into memory of the backend and writes neutral values where the draw left optional attributes out.
---

An API with a fixed vertex input, such as Vulkan, reads every attribute of every vertex.
`VertexFill` makes the bytes that a draw did not write safe to read: the texture coordinate `(0, 0)` and the normal `(0, 0, 1)`.
It leaves the color as it is: the translated shader takes the current color of the render state when the buffer has no colors.

Learn: [Vertices](/build/pipeline/#vertices).

## complete(VertexBuffer,ByteBuffer)

Copies the vertices to `target` from its position, then writes the missing texture coordinates and normals there.
Returns `target`, whose position stays unchanged.
