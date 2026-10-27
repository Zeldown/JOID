---
summary: Base class of the text renderers of glyph fonts, which lays out, kerns, styles and shadows the text and leaves the drawing of each glyph to you.
---

[[MsdfTextRenderer]] and [[BitmapTextRenderer]] extend it.
Extend it for a [[GlyphFont]] of your own: pass the core shader that draws your glyphs to the constructor and implement `drawGlyph`.
On each draw, the renderer lays out the string with [[TextMarkup]], runs the text effects, draws the shadow then the glyphs between `begin()` and `end()`, inside a pushed render state.
The shader is created on the first draw; when the backend cannot compile it, the text is skipped with one dev-mode warning and its bounds are still returned.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## GlyphTextRenderer(CoreShader)

Creates the renderer with the core shader that draws its glyphs.

## getWidth(String,TextInfo)

Returns the width of the laid-out string.

## getHeight(String,TextInfo)

Returns the line height: a glyph font draws one line.

## drawText(double,double,String,TextInfo)

Draws the string with its top-left corner at `x`, `y`.
Throws `IllegalArgumentException` when the font of `info` is not drawn by this renderer.

## drawText(double,double,String,TextInfo,double,double,double,double)

Draws the string as a part of the run `runX`, `runY`, `runWidth`, `runHeight`, over which a gradient color spreads.

## getLineHeight(TextInfo)

Returns [[TextInfo#lineHeight]] times the font size when it is set, otherwise the line height of the face times the font size.

## getBaseline(TextInfo)

Returns the distance from the top of the line to the baseline: the face's own baseline, centered in a line height that differs from the face's.

## layout(String,TextInfo)

Returns the [[GlyphLayout]] of the string: markup tags read, faces resolved by weight and style, kerning and letter spacing applied.

## begin()

JOID calls it before the glyphs of a pass are drawn: it binds the shader, resets the color and reads the pixel grid.
An override binds its texture or uniforms and calls `super.begin()` first.

## end()

JOID calls it after the glyphs of a pass, even when a glyph throws: it unbinds the shader.
An override calls `super.end()`.

## drawGlyph(TextGlyph)

JOID calls it for each glyph of a pass, between `begin()` and `end()`.
Draw the glyph at its x plus offset, on its baseline, at its size, in its color through `bindColor`, and slant it when [[TextGlyph#isSlanted]] is `true`.

## isGridAligned()

If `true`, the origin, the baseline and the offsets of the glyphs snap to whole screen pixels, for pixel fonts; `false` by default.

## bindColor(Color)

Sends `color` to the shader, a gradient spread over the run included; an unchanged color instance sends nothing.

## bindPixel(float,float)

Sends the size of one screen pixel to the `pixel` uniform of the shader; an unchanged value sends nothing.

## getShader()

Returns the shader of the renderer, `null` before the first draw.

## getGrid()

Returns the pixel grid of the render bridge, read in `begin()`.
