---
summary: Reads the glyphs of a font with the font of Java: codes, advances, line metrics and outlines.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## load(byte[])

Returns the font of the bytes.

## code(Font,int)

Returns the glyph code of the code point, `0` when the font has none.

## advance(Font,int)

Returns the advance of the code point, in ems.

## metrics(Font)

Returns the line height, the ascender, the descender, the underline position and the underline thickness, in ems.

## outline(Font,int)

Returns the outline of the code point, with its contours oriented.
