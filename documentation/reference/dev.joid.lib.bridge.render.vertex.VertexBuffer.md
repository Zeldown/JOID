---
summary: The vertices of one draw, in the fixed 32-byte layout that every backend reads.
---

The [[Tessellator]] fills a `VertexBuffer` and hands it to `draw(...)` of the render bridge.
Every vertex takes `STRIDE` bytes, laid out as [[VertexAttribute]] describes: position, texture coordinate, color, normal.
The flags tell which optional attributes the draw wrote: the backend gives the others the texture coordinate `(0, 0)` and the normal `(0, 0, 1)`, or completes them with [[VertexFill]], and the translated shader takes the current color of the render state for a buffer without colors.

Learn: [Vertices](/build/pipeline/#vertices).

## of(ByteBuffer,int,boolean,boolean,boolean)

Wraps `count` vertices that start at the position of `buffer`; `texture`, `color` and `normal` tell whether the draw wrote each optional attribute.

## getBuffer()

Returns the bytes of the vertices, from the current position, in native byte order.

## getCount()

Returns the number of vertices; the render bridge skips a buffer without vertices.

## isTexture()

If `true`, every vertex carries a texture coordinate.

## isColor()

If `true`, every vertex carries its own color; if `false`, the whole draw takes the current color of the render state.

## isNormal()

If `true`, every vertex carries a normal, which the lighting of a 3D model reads.

## STRIDE

The size of one vertex, in bytes.
