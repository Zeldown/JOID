---
summary: Outlines the shape of the previous result with a colored stroke that follows its alpha, rounded corners included.
---

The stroke follows the edge of what was drawn, so it wraps a rounded or circular shape drawn by an earlier pass.

Learn: [Shaders](/use/learn/going-further/shaders/).

## create(Color,float)

Creates a border of `color`, gradient and animated colors included, `width` design pixels wide.

## BorderShaderPass(Color,float)

Creates the pass, for a subclass.

## corners

If `true`, the stroke also covers the outer corners of the area; if `false`, the corner zones, where the stroke goes past the area in x and in y at once, keep the previous result unchanged.

## mode

Where the stroke sits: [[BorderShader.BorderMode#OUT]] outside the edge of the shape, [[BorderShader.BorderMode#IN]] inside it.

## bind(ShaderPassContext)

Binds the border shader with the width in screen pixels, the color and the area.

## unbind()

Unbinds the border shader.

## expansion()

Returns the width plus 2 design pixels, the room an outer stroke needs.

## reach(double)

Returns twice the width in screen pixels, plus one pixel.
