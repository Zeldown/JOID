---
summary: Keeps only the disc of the previous result, with a smooth edge.
---

Learn: [Shaders](/use/learn/going-further/shaders/).

## create(float,float,float)

Creates a circular mask of `radius` centered on `centerX`, `centerY`, in the coordinates you draw in.

## CircleShaderPass(float,float,float)

Creates the pass, for a subclass.

## bind(ShaderPassContext)

Binds the circle shader on the texture of the previous result.

## unbind()

Unbinds the circle shader.
