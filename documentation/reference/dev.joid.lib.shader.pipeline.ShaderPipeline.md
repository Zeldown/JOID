---
summary: Draws something into an offscreen framebuffer, runs it through a chain of shader passes, and draws the result in place.
---

A node effect such as a blur or a rounded mask goes through it; call it yourself in a node layer or a custom node ([Post-process with shader passes](/use/learn/going-further/shaders/#post-process-with-shader-passes)).

The area grows by the largest [[IShaderPass#expansion]] of the passes, and only its part visible on screen is rendered, at the resolution of the screen.
Each pass reads the result of the previous one; the last pass draws on screen with premultiplied blending.
The framebuffers come from [[FrameBufferPool]], a pair per nesting depth, so a pipeline inside a pass gets its own.

Learn: [Shaders](/use/learn/going-further/shaders/).

## render(double,double,double,double,Runnable,IShaderPass[])

Runs `baseDraw` into the area at `x`, `y` of `width` × `height`, in the coordinates you draw in, then applies `passes` in order.
Without passes, or with an area of no width or height, it runs `baseDraw` directly.

## render(double,double,double,double,Runnable,List)

Same as the varargs form, with the passes in a list.
