---
summary: The rectangle of a texture that holds one glyph of a bitmap font, and where that glyph sits in its line.
---

[[BitmapTextRenderer#getCell]] returns one per glyph.
The texel coordinates pick the glyph in the texture; the bounds place it, in units of the bitmap size, from the left edge of the glyph and from the top of its ascender.
Each texel is filtered by the area it covers on screen, so the glyph stays crisp at its bitmap size and smooth at other sizes.

Learn: [Fonts](/use/learn/going-further/fonts/).

## create(ITexture,int,int,int,int)

Creates the cell of the texels from `texelLeft`, `texelTop` included to `texelRight`, `texelBottom` excluded in `texture`; its bounds start at the size of those texels, from `(0, 0)`.
Throws `IllegalArgumentException` when the rectangle holds no texel.

## bold

If `true`, the glyph draws twice, the second time one bitmap unit to the right, which thickens a font that has no bold face.

## grayscale

If `true`, the red channel of the texels is the coverage of a white glyph, tinted by the text color; if `false`, the texels are colors with alpha, multiplied by the text color.

## bounds(double,double,double,double)

Sets where the glyph draws, in bitmap units: `left` and `right` from the left edge of the glyph, `top` and `bottom` from the top of the ascender.
Bounds larger than the texels stretch them.
Throws `IllegalArgumentException` when the bounds are empty.

## getWidth()

Returns the width of the bounds, in bitmap units.

## getHeight()

Returns the height of the bounds, in bitmap units.

## getTexelWidth()

Returns the width of the texel rectangle.

## getTexelHeight()

Returns the height of the texel rectangle.

## getTexelTop()

Returns the top row of the texel rectangle, included.

## getTexelBottom()

Returns the bottom row of the texel rectangle, excluded.

## getTexelLeft()

Returns the left column of the texel rectangle, included.

## getTexelRight()

Returns the right column of the texel rectangle, excluded.

## getTexture()

Returns the texture that holds the glyph.

## getTop()

Returns the top of the bounds, in bitmap units from the top of the ascender.

## getBottom()

Returns the bottom of the bounds, in bitmap units from the top of the ascender.

## getLeft()

Returns the left of the bounds, in bitmap units from the left edge of the glyph.

## getRight()

Returns the right of the bounds, in bitmap units from the left edge of the glyph.
