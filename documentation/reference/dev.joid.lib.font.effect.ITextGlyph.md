---
summary: One laid-out glyph as an ITextEffect sees it, with its place, its metrics in design pixels, and the codepoint, color and offset an effect can change.
---

Positions and metrics are in design pixels, scaled by the font size of the glyph.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## codepoint

The character that the glyph draws: `getCodepoint()` reads it, `codepoint(int)` replaces it, for example to scramble a text; the advance of the glyph is unchanged.

## color

The color the glyph draws in: `getColor()` reads it, `color(Color)` replaces it.

## getX()

Returns the left edge of the glyph, before its offset.

## getIndex()

Returns the index of the glyph's character in the string, markup tags included, so that it stays stable while the text is typed.

## getSize()

Returns the font size of the glyph, in design pixels.

## getAdvance()

Returns the distance from this glyph to the next one, kerning and letter spacing included.

## getAdvance(int)

Returns the advance of `codepoint` in the face of this glyph, at its size.

## getOffsetX()

Returns the horizontal offset that the effects gave the glyph.

## getOffsetY()

Returns the vertical offset that the effects gave the glyph.

## getAscender()

Returns the height of the face above the baseline, at the size of the glyph.

## getDescender()

Returns the depth of the face under the baseline at the size of the glyph, negative below it.

## getBaseline()

Returns the y of the baseline of the glyph.

## getUnderlineY()

Returns the y where an underline of the face sits.

## getStyle()

Returns the [[TextStyle]] that the glyph was laid out with: weight, italic, color and effects.

## getUnderlineThickness()

Returns the thickness of an underline of the face, at the size of the glyph.

## isShadow()

If `true`, this glyph is the copy drawn as the shadow of the text.

## hasGlyph(int)

If `true`, the face of this glyph can draw `codepoint`.

## offset(double,double)

Moves the glyph by `x`, `y` from its place, without moving the next glyphs.
