---
summary: An effect on the glyphs of a text, which can move, recolor or replace each glyph and draw behind or over it.
---

Give effects to a [[TextInfo]] with `effects(...)`, or add them from a markup tag with [[TextStyle#effect]].
JOID calls the three hooks for every glyph, on the render thread, in this order: `apply` for each glyph, then `background`, then the glyphs are drawn, then `decorate`.
A shadow glyph goes through `decorate` only: test [[ITextGlyph#isShadow]] there to skip it.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## apply(ITextGlyph)

JOID calls it once per glyph before anything is drawn.
Change the glyph there: its offset, its color or its codepoint; reading [[ITextGlyph#getIndex]] and the clock gives per-character animations such as a wave.

## background(ITextGlyph)

JOID calls it once per glyph after `apply`, before the glyphs are drawn: draw a highlight or a box behind the glyph, in design pixels, with [[DrawUtils]].

## decorate(ITextGlyph)

JOID calls it once per glyph after the glyphs are drawn: draw an underline, a strike-through or a cursor over the glyph.
