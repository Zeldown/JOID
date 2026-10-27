---
summary: One face of an MsdfFont, with its atlas texture, its metrics, its glyphs and its kerning.
---

An [[IMsdfSource]] reads it; [[MsdfFontLoader]] does that for you.
Metrics, advances and kerning are on 1 of the font size.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/), [MSDF text](/use/in-depth/msdf-text/).

## of(MsdfAtlas,MsdfMetrics,Map,Map,BufferedImage,String,FontWeight,boolean)

Creates a face from its atlas, its metrics, its glyphs by codepoint, its kerning pairs keyed by `pair(previous, current)`, the atlas image, its name, its weight and its style.
The image becomes a linear texture without mipmaps, uploaded the first time the face draws.
Throws `IllegalArgumentException` when `name` is empty.

## getAscender()

Returns the ascender of the face, on 1 of the font size.

## getDescender()

Returns the descender of the face, on 1 of the font size, negative below the baseline.

## getLineHeight()

Returns the line height of the face, on 1 of the font size.

## getUnderlineY()

Returns the position of an underline relative to the baseline, on 1 of the font size.

## getUnderlineThickness()

Returns the thickness of an underline, on 1 of the font size.

## getAdvance(int)

Returns the advance of `codepoint`, on 1 of the font size, `0F` for a codepoint the face does not have.

## getKerning(int,int)

Returns the kerning between `previous` and `current`, on 1 of the font size, `0F` for none.

## hasGlyph(int)

If `true`, the face has a glyph for `codepoint`, a space included.

## getGlyph(int)

Returns the [[MsdfGlyph]] of `codepoint`, or `null` when the face does not have it.

## getGlyphs()

Returns the glyphs by codepoint, as a map you cannot change.

## getKerningPairs()

Returns the kerning by pair of codepoints, keyed by `pair(previous, current)`, as a map you cannot change.

## getName()

Returns the name of the font of this face.

## getXHeight()

Returns the height of a lowercase `x`, on 1 of the font size, `0F` when the face has none.

## isItalic()

If `true`, the face is italic.

## getAtlas()

Returns the size and the distance range of the atlas.

## getTexture()

Returns the atlas as a [[Resource]].

## getWeight()

Returns the weight of the face.

## getMetrics()

Returns the vertical metrics of the face.

## pair(int,int)

Returns the key of the kerning pair `previous`, `current` in `getKerningPairs()`.

## style(FontWeight,boolean)

Returns the same face, sharing its atlas and glyphs, declared with another weight and style.
