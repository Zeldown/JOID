---
summary: The helpers of the contract suites, for contract tests of your own: a frame around a drawing, quads and vertices, a shader from a fragment stage, a pixel assertion.
---

Learn: [The suites](/build/testing-a-backend/#the-suites).

## frame(ISnapshotBackend,int,int,Consumer)

Begins a frame, clears it to opaque black, runs the drawing, ends the frame, then returns the capture of that size and presents.

## reset(int,int)

Resets the modelview, an orthographic projection of that size from the top-left corner, and a render state with that viewport, normal blending and no depth writes.

## shader(String)

Returns a shader of that fragment stage, with a vertex stage that applies the projection and the modelview.

## assertPixel(SnapshotImage,int,int,int)

Fails with both colors in hexadecimal when the pixel at that position is not that ARGB color.

## quad(float,float,float,float,float,boolean,int,boolean,float,float)

Returns two triangles of that rectangle at that depth, with texture coordinates from `start` to `end`, and an ARGB vertex color when it is not `0`.

## vertices(float[][],float,boolean,int,boolean)

Returns a vertex buffer of those points, each `{x, y, u, v}`, at that depth, with the texture coordinates, the color and the normal flags of the buffer.
