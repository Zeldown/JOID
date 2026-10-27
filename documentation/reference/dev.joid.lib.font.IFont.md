---
summary: A font that JOID can draw, which names the text renderer that measures and draws it.
---

[[MsdfFont]] and [[BitmapFont]] implement it, through [[GlyphFont]]; implement it yourself for a font that another renderer draws.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## getTextRenderer()

Returns the [[ITextRenderer]] that measures and draws this font; every [[TextInfo]] with this font goes through it.
