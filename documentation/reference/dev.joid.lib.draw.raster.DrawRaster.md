---
summary: Shows what another renderer draws inside a UI, through an offscreen framebuffer drawn as an image.
---

Reach it through `DrawUtils.RASTER`.
The framebuffer has the size of the area in screen pixels, at most 4096 on each side, so the result stays sharp at every interface scale.
JOID keeps one framebuffer per nesting depth, grows it when an area does not fit and frees it after 5 seconds without use.

Learn: [Drawing shapes and text](/use/learn/going-further/drawing/).

## inst()

Returns the shared instance, the one in `DrawUtils.RASTER`.

## drawRaster(double,double,double,double,DrawableRaster)

Runs `drawable` into the offscreen framebuffer, then draws that framebuffer at `x`, `y`, `width`, `height` with premultiplied blending.
An area without width or height draws nothing; a raster drawn inside another one gets its own framebuffer.
