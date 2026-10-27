---
summary: The vertex buffer of the OpenGL render bridge, with the vertex array that points every attribute of JOID at it.
---

Without vertex arrays, the attributes are pointed again at each draw.

Learn: [Capabilities and paths](/build/base-modules/opengl/#capabilities-and-paths).

## GlVertexInput(IGlBinding,boolean)

Creates the buffer, and a vertex array whose attributes point at it when the boolean is `true`.

## bind()

Binds the vertex array and the buffer, or points the attributes at the buffer without a vertex array.

## getArray()

Returns the OpenGL name of the vertex array, or `0` without one.

## getBuffer()

Returns the OpenGL name of the vertex buffer.

## getBinding()

Returns the binding that the input calls.
