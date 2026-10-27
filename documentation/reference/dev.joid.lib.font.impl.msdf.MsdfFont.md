---
summary: A font drawn from multi-channel signed distance fields, sharp at every size, with one face per weight and style.
---

[[MsdfFontLoader]] creates it; pass it to [[TextInfo#create]].
Its glyphs are drawn by [[MsdfTextRenderer]].

Learn: [Fonts](/use/learn/describing-the-ui/fonts/), [MSDF text](/use/in-depth/msdf-text/).

## of(MsdfFontFace[])

Creates a font from faces that are already read.
Throws `IllegalArgumentException` when no face is given or when two faces share a weight and a style.

## getTextRenderer()

Returns [[MsdfTextRenderer#inst]].
