---
summary: The shader of discs, which masks what is drawn outside a circle with a smooth edge.
---

[[DrawShape#drawCircle]] and [[CircleShaderPass]] use it.

Learn: [Shaders](/use/learn/going-further/shaders/).

## inst()

Returns the shared shader.

## use(float,float,float,Runnable)

Runs `runnable` with the shader bound on the circle of `radius` centered on `centerX`, `centerY`, then restores the previous shader.

## bind(float,float,float)

Binds the shader on the circle, masking the texture tinted by the color.

## bind(float,float,float,ShaderFillType)

Binds the shader on the circle and masks what `type` names.
