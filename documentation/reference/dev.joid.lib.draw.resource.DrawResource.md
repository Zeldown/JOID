---
summary: Draws resources and GPU textures, tinted by the current color of the render state.
---

Reach it through `DrawUtils.RESOURCE`.
Positions and sizes are in design pixels, in the coordinates you draw in.
The image is multiplied by the current color of the render state: bind a [[Color]] around the call to tint it or fade it.

A drawn resource asks its decoder for the size it covers in screen pixels, so a vector image stays sharp, and turns on mipmaps by itself when it is drawn smaller than its texture with linear filtering.
A resource that is still loading draws nothing; a failed resource draws a checkerboard in dev mode and nothing otherwise.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/).

## inst()

Returns the shared instance, the one in `DrawUtils.RESOURCE`.

## drawResource(double,double,Resource)

Draws the resource at `x`, `y` at its own size, [[Resource#getWidth]] × [[Resource#getHeight]].

## drawResource(double,double,double,double,Resource)

Draws the resource stretched to `width` × `height`.

## drawResource(double,double,double,double,double,double,double,double,Resource)

Draws a region of the image stretched to `width` × `height`: the region starts at `u`, `v` and spans `regionWidth` × `regionHeight`, in pixels of the source image.
When the resource has its own region (`textureCoords`), `u` and `v` count from the corner of that region.

## drawTexture(double,double,double,double,ITexture,double,double,double,double,TextureFilter,BlendState)

Draws a raw GPU texture, such as the color texture of a [[FrameBuffer]], stretched to `width` × `height`, with the filter and the blending you pass.
`u0`, `v0` and `u1`, `v1` are the texture coordinates of the top-left and bottom-right corners, on 1; swap `v0` and `v1` to flip the image.
