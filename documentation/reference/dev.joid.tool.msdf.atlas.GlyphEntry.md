---
summary: One glyph of an atlas being generated: its code point, advance and outline, then its place and size in the atlas.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## GlyphEntry(int,double,Shape)

Creates the entry of a code point, with its advance in ems and its outline.

## getX()

Returns the left edge of the glyph in the atlas, in pixels.

## setX(int)

Sets the left edge of the glyph in the atlas.

## getY()

Returns the top edge of the glyph in the atlas, in pixels.

## setY(int)

Sets the top edge of the glyph in the atlas.

## getWidth()

Returns the width of the glyph in the atlas, in pixels, margin included.

## getHeight()

Returns the height of the glyph in the atlas, in pixels, margin included.

## getShape()

Returns the outline.

## getCodepoint()

Returns the code point.

## getAdvance()

Returns the advance, in ems.

## getBounds()

Returns the bounds of the outline, `{left, bottom, right, top}` in ems, or `null` for a glyph without outline.

## getTop()

Returns the top of the area of the glyph, in ems, margin included.

## getBottom()

Returns the bottom of the area of the glyph, in ems, margin included.

## getLeft()

Returns the left of the area of the glyph, in ems, margin included.

## getRight()

Returns the right of the area of the glyph, in ems, margin included.

## isDrawable()

If `true`, the glyph has an outline to draw; a space has none.

## measure(double,double)

Computes the area of the glyph at that size in pixels per em, with a margin of half the distance range on each side.
