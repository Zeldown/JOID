---
summary: The shader of rounded rectangles and rounded borders, which masks what is drawn outside a rounded box.
---

[[DrawShape]] and [[RoundedShaderPass]] use it; call it for geometry of your own that needs rounded corners.
The box is given as its inner rectangle, the box shrunk by the radius on each rounded side, in the coordinates you draw in.

Learn: [Shaders](/use/learn/going-further/shaders/).

## inst()

Returns the shared shader.

## use(float,float,float,float,float,Runnable)

Runs `runnable` with the shader bound on the inner rectangle `x1`, `y1`, `x2`, `y2` and the corner `radius`, then restores the previous shader.

## bind(float,float,float,float,float)

Binds the shader on the inner rectangle and the corner radius, masking the texture tinted by the color.

## bind(float,float,float,float,float,ShaderFillType)

Binds the shader and masks what `type` names.

## aligned(boolean)

If `true`, the edge is smoothed over one screen pixel for a box on the pixel grid; if `false`, over the coverage of a box under a rotation or a fractional scale.

## stroke(float)

Keeps only a stroke `stroke` wide inside the edge, for a rounded border; `0F` fills the whole box.
