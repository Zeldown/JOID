---
summary: Measures and draws the strings of one kind of font.
---

[[DrawText]] and [[TextInfo]] reach it through [[IFont#getTextRenderer]].
Implement it for a font that is not made of glyphs; for a font of glyphs, extend [[GlyphTextRenderer]], which lays out, kerns and styles the text for you.
Positions and sizes are in design pixels.

Learn: [Fonts](/use/learn/going-further/fonts/).

## drawText(double,double,String,TextInfo)

Draws `text` on one line with its top-left corner at `x`, `y`, and returns its bounds.
JOID calls it on the render thread, during a frame.

## drawText(double,double,String,TextInfo,double,double,double,double)

Draws `text` as a part of a longer run whose box is `runX`, `runY`, `runWidth`, `runHeight`, so that a gradient spreads over the whole run.
By default it ignores the run and calls `drawText(x, y, text, info)`.

## getLineHeight(TextInfo)

Returns the height of one line with `info`.

## getWidth(String,TextInfo)

Returns the width of `text` on one line with `info`, markup tags excluded.

## getHeight(String,TextInfo)

Returns the height of `text` with `info`.

## getBaseline(TextInfo)

Returns the distance from the top of a line to its baseline with `info`; the elements of a [[Text]] align on it.
By default, the line height: the baseline sits at the bottom of the line.
