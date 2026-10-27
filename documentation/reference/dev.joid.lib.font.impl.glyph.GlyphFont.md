---
summary: Base class of the fonts made of glyphs, which hold a FontFamily of faces.
---

[[MsdfFont]] and [[BitmapFont]] extend it.
Extend it with your own [[IFontFace]] and [[GlyphTextRenderer]] for another kind of glyph font; `getTextRenderer()` must return a renderer that draws this class.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## GlyphFont(FontFamily)

Creates the font from the family of its faces.

## getFace(FontWeight,boolean)

Returns the face that draws `weight` and `italic`, resolved by [[FontFamily#resolve]].

## getFamily()

Returns the family of faces of this font.
