---
summary: Base class of the text renderers of pixel fonts, which draws each glyph from the `BitmapCell` you return.
---

Extend it for your [[BitmapFont]] and implement `getCell`; layout, markups, effects, shadows and colors come from [[GlyphTextRenderer]].
The origin, the baseline and the offsets of the glyphs snap to whole screen pixels, and the texture of each cell is sampled with `NEAREST` filtering and filtered by area in the shader.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## BitmapTextRenderer()

Creates the renderer with the bitmap shader of JOID.

## drawGlyph(TextGlyph)

Draws the cell of the glyph, scaled from its bitmap size to the size of the glyph, slanted when the style is italic and the face is not.
A glyph whose cell is `null` draws nothing.

## begin()

Binds the shader and starts the pass with no texture bound.

## isGridAligned()

Always `true`: bitmap glyphs sit on whole screen pixels.

## getCell(TextGlyph)

JOID calls it for every glyph it draws, on the render thread.
Return the [[BitmapCell]] of the glyph's codepoint in its face, or `null` to skip it.
