---
summary: Places the glyphs of an atlas in rows and finds the largest glyph size that fits.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## pack(List,double,double,int,int)

Measures and places every drawable glyph at that size, tallest first, with one pixel between glyphs; returns `false` when they do not fit in the atlas.

## fit(List,double,int,int)

Returns the largest size, to a sixteenth of a pixel, at which the glyphs fit, and leaves them placed at it.
Throws an `IllegalStateException` when they do not fit even at 1 pixel.
