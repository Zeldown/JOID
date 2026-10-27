---
summary: The drawing of another renderer, which JOID runs into an offscreen framebuffer and then shows as an image.
---

Pass it to [[DrawRaster#drawRaster]] as a lambda.

Learn: [Drawing shapes and text](/use/learn/going-further/drawing/).

## draw(int,int)

JOID calls it on the render thread with the framebuffer bound, cleared to transparent, and a viewport of `width` × `height` screen pixels.
Draw there with your own graphics code, with premultiplied alpha; the projection maps one unit to one pixel, with `(0, 0)` at the top-left corner.
