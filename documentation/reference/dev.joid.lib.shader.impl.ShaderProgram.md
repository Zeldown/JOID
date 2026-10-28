---
summary: Base class of a shader of your own, loaded from JOID GLSL sources and created on the render bridge the first time it is used.
---

Extend it with a singleton, load the sources in the constructor, and draw through `use` so that the previous shader and blending come back afterwards ([Write a shader](/use/learn/going-further/shaders/#write-a-shader)).

A source that cannot be read or compiled warns once and makes the shader unavailable: what it draws is skipped instead of failing the frame.
Create and use it on the render thread.

Learn: [Shaders](/use/learn/going-further/shaders/).

## ShaderProgram()

Creates a program, for a subclass.

## delete()

Frees the shader on the GPU; the next use creates it again.

## bind()

Binds the shader when it is available.

## unbind()

Unbinds the shader.

## getShader()

Returns the shader of the render bridge, created on the first call, or `null` when the sources could not be loaded or compiled.

## canDraw()

Returns `isAvailable()` and, when it is `false`, warns once in dev mode that what the shader draws is skipped.

## isAvailable()

If `true`, the shader is created and active on the current render bridge.

## gradient(ColorGradient,BoundingBox)

Sends `gradient` to the gradient uniforms of the shader, spread over `canvas`.

## use(Runnable,Runnable)

Runs `bind`, which binds the shader and sets its uniforms, then `body`, which draws; then unbinds the shader and restores the previous shader and blending, even when `body` throws.
Does nothing when the shader is unavailable.

## uniformGradient(IShader,ColorGradient,float,float,float,float)

Sets `u_HasGradient` to 1 and the gradient uniforms of `shader` from `gradient`, spread over the box `x1`, `y1`, `x2`, `y2`.

## uniformGradient(IShader,Point,Point,Color,Color,float,float,float,float)

Sets the gradient uniforms of `shader` (`u_GradientStart`, `u_GradientEnd`, `u_GradientStartPos`, `u_GradientEndPos`, `u_GradientCanvas`) from two stops and two points on 1, resolving animated colors, spread over the box `x1`, `y1`, `x2`, `y2`.

## load(Object,Object)

Reads the vertex and fragment sources from anything [[Asset#of]] accepts; the shader is created later, at its first use.
A source that cannot be read warns with the cause and leaves the shader unavailable.
