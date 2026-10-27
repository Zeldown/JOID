---
summary: The snapped edges of a shape along one axis, with the coverage that fades a span thinner than a pixel.
---

`spanX` and `spanY` of [[PixelGrid]] return it.
Draw between `getStart()` and `getEnd()`, and multiply the alpha of the color by `getCoverage()`.

Learn: [Pixel alignment](/use/in-depth/pixel-alignment/#thin-lines-keep-their-weight).

## getStart()

Returns the design coordinate of the near edge, on a pixel boundary when the grid is aligned.

## getEnd()

Returns the design coordinate of the far edge, on a pixel boundary when the grid is aligned.

## getCoverage()

Returns the share of the drawn pixels that the shape covers, from `0F` to `1F`: below `1F` only for a span thinner than one screen pixel.
