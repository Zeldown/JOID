---
summary: One glyph of an MSDF face, with its advance and where it sits on the line and in the atlas.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## of(int,float,MsdfBounds,MsdfBounds)

Creates the glyph of `codepoint` with its `advance` on 1 of the em, its plane bounds and its atlas bounds; a glyph without bounds, such as a space, has `null` for both.

## getCodepoint()

Returns the character of the glyph.

## getAdvance()

Returns the distance to the next glyph, on 1 of the em.

## getPlaneBounds()

Returns the bounds of the glyph around its origin on the baseline, on 1 of the em with Y up, or `null` for a glyph that draws nothing.

## getAtlasBounds()

Returns the bounds of the glyph in the atlas image, in pixels, or `null` for a glyph that draws nothing.
