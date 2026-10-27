---
summary: Keeps only a rounded rectangle of the previous result, with smooth corners.
---

Learn: [Shaders](/use/learn/going-further/shaders/).

## create(float,float,float,float,float)

Creates a rounded mask whose inner rectangle runs from `x1`, `y1` to `x2`, `y2`, in the coordinates you draw in, and whose edge lies `radius` outside it.
To round a box of `left`, `top`, `right`, `bottom`, pass `left + radius`, `top + radius`, `right - radius`, `bottom - radius`; a side left at its edge keeps square corners.

## RoundedShaderPass(float,float,float,float,float)

Creates the pass, for a subclass.

## bind(ShaderPassContext)

Binds the rounded shader on the texture of the previous result.

## unbind()

Unbinds the rounded shader.
