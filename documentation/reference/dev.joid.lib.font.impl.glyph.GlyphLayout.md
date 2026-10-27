---
summary: A string laid out on one line, as the list of its glyph placements and its width.
---

[[GlyphTextRenderer#layout]] builds it: markup tags read, faces resolved, kerning and letter spacing applied.

Learn: [Fonts](/use/learn/going-further/fonts/).

## of(List,double)

Creates a layout from its placements and its width.

## getPlacements()

Returns the placements, in the order of the string, as a list you cannot change; characters that the face cannot draw are left out, except spaces.

## getWidth()

Returns the width of the line in design pixels, without the letter spacing after the last glyph.
