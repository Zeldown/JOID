---
summary: The shader of the one-axis blur that BlurShaderPass runs.
---

Learn: [Shaders](/use/learn/going-further/shaders/).

## inst()

Returns the shared shader.

## bind(float,float,float,float,float)

Binds the shader with a `radius` in texels, the direction `dirX`, `dirY` (`1, 0` horizontal, `0, 1` vertical) and the size of one texel `texelW`, `texelH` on 1.
