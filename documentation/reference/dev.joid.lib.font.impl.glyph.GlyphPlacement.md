---
summary: The place of one character in a GlyphLayout, with the font, the face and the style it draws with.
---

Learn: [Fonts](/use/learn/going-further/fonts/).

## of(int,int,GlyphFont,F,double,TextStyle)

Creates the placement of `codepoint`, found at `index` of the string, drawn with `face` of `font` in `style`, at `x` from the start of the line.

## getIndex()

Returns the index of the character in the string, markup tags included.

## getCodepoint()

Returns the character.

## getFont()

Returns the [[GlyphFont]] that draws the character; a markup can switch it.

## getFace()

Returns the face that draws the character.

## getX()

Returns the distance from the start of the line to the left edge of the glyph, in design pixels.

## getStyle()

Returns the [[TextStyle]] at this character.
