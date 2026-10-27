---
summary: The ITextGlyph that a GlyphTextRenderer lays out and draws, with the face and the font it comes from.
---

[[GlyphTextRenderer]] creates one per drawn character, passes it to the text effects, then to its `drawGlyph` hook.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## create(GlyphFont,F,int,int,TextStyle,double,double,double,double,Color)

Creates a glyph of `codepoint` at index `index` of the string, drawn with `face` of `font` in `style`, its left edge at `x`, on the baseline `baseline`, at `size` design pixels, with `advance` to the next glyph and the color `color`.

## codepoint

The character that the glyph draws: `getCodepoint()` reads it, `codepoint(int)` replaces it.

## color

The color the glyph draws in: `getColor()` reads it, `color(Color)` replaces it.

## getX()

Returns the left edge of the glyph, before its offset.

## getSize()

Returns the font size of the glyph, in design pixels.

## isSlanted()

If `true`, the style is italic and the face is not, so the renderer slants the glyph by itself.

## getAdvance(int)

Returns the advance of `codepoint` in the face, at the size of the glyph.

## getAdvance()

Returns the distance from this glyph to the next one.

## getAscender()

Returns the ascender of the face at the size of the glyph.

## getDescender()

Returns the descender of the face at the size of the glyph, negative below the baseline.

## getUnderlineY()

Returns the y where an underline of the face sits.

## getUnderlineThickness()

Returns the thickness of an underline of the face at the size of the glyph.

## hasGlyph(int)

If `true`, the face can draw `codepoint`.

## getFace()

Returns the [[IFontFace]] that draws the glyph.

## getIndex()

Returns the index of the glyph's character in the string, markup tags included.

## isShadow()

If `true`, this glyph is the shadow copy of another one.

## getBaseline()

Returns the y of the baseline of the glyph.

## getStyle()

Returns the [[TextStyle]] the glyph was laid out with.

## getFont()

Returns the [[GlyphFont]] the glyph comes from.

## getOffsetX()

Returns the horizontal offset that the effects gave the glyph.

## getOffsetY()

Returns the vertical offset that the effects gave the glyph.

## shadow(double,double,Color)

Returns the shadow copy of this glyph, moved by `x`, `y` and drawn in `color`.

## offset(double,double)

Moves the glyph by `x`, `y` from its place; on the pixel grid the renderer rounds the offset to whole screen pixels.
