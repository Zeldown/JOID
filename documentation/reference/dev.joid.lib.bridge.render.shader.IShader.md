---
summary: A shader program of the render bridge, with its uniforms and samplers, bound through the render state.
---

Learn: [Shaders](/use/learn/going-further/shaders/), [Shader translation](/build/shader-translation/).

## delete()

Releases the program and its line variant.

## bind()

Makes the shader and its blend state the current ones of the render state.

## unbind()

Removes the shader from the render state when it is current, and puts the previous blend state back.

## isBound()

If `true`, the shader is bound.

## isActive()

If `true`, the shader compiled and can draw; the first call compiles it.

## getLineShader()

Returns the variant of the shader that draws wide lines as triangles, with the same uniforms and samplers.

## uniform(String,boolean)

Sets a `bool` uniform, sent at the next draw.

## uniform(String,int[])

Sets an integer uniform or array, sent at the next draw.

## uniform(String,float[])

Sets a float uniform, vector, matrix or array, sent at the next draw.

## sampler(String,ITexture,TextureFilter,TextureWrap)

Sets the texture of a sampler, with its filter and wrap.
Throws an `IllegalArgumentException` that lists the samplers when the shader declares no sampler of that name.
