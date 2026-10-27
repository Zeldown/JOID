---
summary: What a shader pass needs to know about the area it runs on, the part of it rendered and the framebuffer that holds it.
---

[[ShaderPipeline]] builds it and passes it to [[IShaderPass#bind]].
The area is in the coordinates you draw in; the region is the part of the expanded area that is visible on screen, which is what the framebuffer holds.

Learn: [Shaders](/use/learn/going-further/shaders/).

## of(double,double,double,double,double,double,PixelGrid)

Creates the context of the area at `x`, `y` of `width` × `height`, grown by `expansion` design pixels, rendering the part visible on `grid` plus `margin` screen pixels.

## getX()

Returns the left of the area, before the expansion.

## getY()

Returns the top of the area, before the expansion.

## getWidth()

Returns the width of the area, before the expansion.

## getHeight()

Returns the height of the area, before the expansion.

## getTexelWidth()

Returns the width of one texel of the framebuffer, on 1: use it to step by one pixel in a pass.

## getTexelHeight()

Returns the height of one texel of the framebuffer, on 1.

## getExpansion()

Returns how far the area grows on each side, in design pixels.

## getGrid()

Returns the pixel grid of the screen, which gives the scale from design pixels to screen pixels.

## getRegionX()

Returns the left of the rendered region.

## getRegionY()

Returns the top of the rendered region.

## getRegionWidth()

Returns the width of the rendered region.

## getRegionHeight()

Returns the height of the rendered region.

## getTextureWidth()

Returns the width of the rendered region, in screen pixels.

## getTextureHeight()

Returns the height of the rendered region, in screen pixels.

## getFrameBufferWidth()

Returns the width of the framebuffer, at least the texture width.

## getFrameBufferHeight()

Returns the height of the framebuffer, at least the texture height.

## withFrameBuffer(int,int)

Returns the same context for a framebuffer of `frameBufferWidth` × `frameBufferHeight`.
