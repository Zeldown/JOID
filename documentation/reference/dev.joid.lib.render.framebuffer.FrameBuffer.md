---
summary: An offscreen image that you draw into once and then draw as many times as you want.
---

Fill it with your own drawing, then draw it where you need it; it keeps its content until you fill it again ([Draw once, reuse every frame](/use/learn/going-further/framebuffers-and-models/#draw-once-reuse-every-frame)).

A framebuffer holds GPU memory: create it on the render thread and call `delete()` when you are done with it.

Learn: [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/).

## create(int,int,TextureFilter)

Creates a framebuffer of `width` × `height` screen pixels, drawn later with the filter `filter`.
Its content is undefined until the first `fill`.

## delete()

Frees the GPU memory of the framebuffer; do not use it afterwards.

## fill(Runnable)

Runs `runnable` into the framebuffer, with one unit per pixel and `(0, 0)` at its top-left corner.
It does not clear the framebuffer first: clear it in `runnable` when you redraw over an earlier content.

## fill(double,double,double,double,Runnable)

Runs `runnable` into the framebuffer, with the area at `x`, `y` of `width` × `height` stretched over the whole framebuffer, so that you draw in the coordinates of that area.

## draw(double,double,double,double)

Draws the content at `x`, `y`, stretched to `width` × `height`, with premultiplied blending.
Throws `IllegalStateException` before the first `fill`.

## getWidth()

Returns the width, in pixels.

## getHeight()

Returns the height, in pixels.

## getHandle()

Returns the framebuffer of the render bridge, for drawing it yourself with [[DrawResource#drawTexture]].

## getFilter()

Returns the filter used when the framebuffer is drawn.

## isFilled()

If `true`, the framebuffer has been filled at least once and can be drawn.
