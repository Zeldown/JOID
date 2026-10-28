---
summary: Base class of the pixel fonts, glyph fonts drawn from texture cells at an exact bitmap size.
---

Extend it with a [[BitmapTextRenderer]] of your own that returns the [[BitmapCell]] of each glyph.
At a font size equal to the bitmap size, one font pixel (one texel of a cell unless its `bounds` say otherwise) is one design pixel; other sizes scale the cells.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## BitmapFont(FontFamily,int)

Creates the font from its faces and its bitmap size: the number of font pixels per em, which is the font size in design pixels at which one font pixel draws as one design pixel.
Throws `IllegalArgumentException` when `bitmapSize` is 0 or less.

## getBitmapSize()

Returns the bitmap size of the font.
