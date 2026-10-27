---
summary: One face of a glyph font, a weight and a style, with its metrics on 1 of the font size.
---

[[MsdfFontFace]] implements it; implement it with your own [[GlyphTextRenderer]] for another kind of glyph font.
Every metric is on 1 of the font size: a line height of `1.2F` at a size of 20 design pixels is 24 design pixels.

Learn: [Fonts](/use/learn/going-further/fonts/).

## isItalic()

If `true`, this face is italic.

## getAscender()

Returns the height above the baseline, on 1 of the font size.

## getDescender()

Returns the depth under the baseline, on 1 of the font size, negative below it.

## getLineHeight()

Returns the height of one line, on 1 of the font size.

## getUnderlineY()

Returns the position of an underline relative to the baseline, on 1 of the font size, negative below it.

## getUnderlineThickness()

Returns the thickness of an underline, on 1 of the font size.

## getName()

Returns the name of the face, shown in the warnings.

## getWeight()

Returns the [[FontWeight]] of this face.

## getAdvance(int)

Returns the distance from `codepoint` to the next character, on 1 of the font size.

## getKerning(int,int)

Returns the adjustment between `previous` and `current`, on 1 of the font size, `0F` for none.

## hasGlyph(int)

If `true`, this face can draw `codepoint`; the renderer skips a character it cannot draw, except a space.
