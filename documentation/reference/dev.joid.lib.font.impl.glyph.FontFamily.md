---
summary: The faces of one font, by weight and style, from which a text picks the face it draws with.
---

A [[GlyphFont]] holds one family; [[MsdfFontLoader]] builds it for you from the files you load.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## of(F[])

Creates a family of `faces`, sorted by weight.
Throws `IllegalArgumentException` when no face is given or when two faces share a weight and a style.

## resolve(FontWeight,boolean)

Returns the face for `weight` and `italic`: among the faces of that style, or all faces when none has it, the closest weight.
For a requested weight of 400 or above 500, the heavier of two equally close faces wins; otherwise the lighter one.
A weight that the family does not have warns once in dev mode and names the loaded faces.

## getFaces()

Returns the faces, sorted by weight, as a list you cannot change.
