---
summary: The size of an MSDF atlas image, the em size it was generated at and its distance range.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## of(int,int,float,float)

Creates the description of an atlas of `width` × `height` pixels, whose glyphs were generated at `size` pixels per em with a distance range of `distanceRange` pixels.

## getWidth()

Returns the width of the atlas image, in pixels.

## getHeight()

Returns the height of the atlas image, in pixels.

## getSize()

Returns the em size of the glyphs in the atlas, in pixels.

## getDistanceRange()

Returns the width of the distance field around each edge, in atlas pixels.
