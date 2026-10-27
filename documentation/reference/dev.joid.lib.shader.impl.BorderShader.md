---
summary: The shader of the outline that BorderShaderPass draws around the alpha of a texture.
---

Learn: [Shaders](/use/learn/going-further/shaders/).

## inst()

Returns the shared shader.

## bind(float,Color,float,float,boolean,BorderMode,float,float,float,float)

Binds the shader with a stroke of `borderWidth` texels in `borderColor`, gradient and animated colors included, the size of one texel `texelW`, `texelH` on 1, the `fill` and `mode` of [[BorderShaderPass]], and the area `rectX1`, `rectY1`, `rectX2`, `rectY2`.
