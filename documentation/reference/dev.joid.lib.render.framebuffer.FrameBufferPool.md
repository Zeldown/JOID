---
summary: Lends offscreen framebuffers by owner and nesting depth, grows them when a draw does not fit, and frees the ones left unused.
---

The effects, the shader passes and [[DrawRaster]] borrow their framebuffers here, so that a size that changes on every frame does not create one each time.
A framebuffer grows to the next power of two of the size asked, and is freed after 5 seconds without use.
Each draw of a UI bridge calls `releaseUnused()`, so a host that draws through the UI bridge has nothing to call.

Learn: [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/).

## allocate(Object,int,int,int,int)

Returns `count` framebuffers of at least `width` × `height` pixels for `owner` at nesting depth `depth`, the same ones as last time when they are large enough.
Use one depth per level of nesting, so that a draw inside a draw gets its own framebuffers.

## cleanup()

Frees every pooled framebuffer, for example before the render bridge is destroyed.

## releaseUnused()

Frees the framebuffers that no `allocate` asked for in the last 5 seconds.
