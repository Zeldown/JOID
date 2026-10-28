---
summary: The part of the render state that a pipeline object bakes in, as a value that a backend uses as the key of its pipeline cache.
---

An API with immutable pipelines can bake the blending, the depth, the culling, the color writes and the topology into them; the Vulkan backend keeps the culling, the depth and the stencil dynamic, and keys its cache on the rest.
`PipelineKey` gathers those values for one draw, normalized so that equal pipelines get equal keys: a disabled blending is always `BlendState.DISABLED`, and the depth writes are off without the depth test.
Two keys with the same values are equal and have the same hash.

Learn: [Render pipeline](/build/pipeline/#the-render-state).

## of(IShader,RenderState,Primitive)

Builds the key of a draw of `primitive` with `shader` under `state`.

## stencil(IShader,RenderState,Primitive)

Builds the key of the `WRITE` pass of [[StencilEmulation]]: no blending, no depth test nor depth write, color writes on, and the culling of the state.

## getShader()

Returns the shader of the draw.

## getBlend()

Returns the blending, `BlendState.DISABLED` when the state does not blend.

## isColorWrite()

If `true`, the draw writes the color channels.

## isDepthTest()

If `true`, the draw compares its depth with the stored one.

## isDepthWrite()

If `true`, the draw writes its depth; always `false` without the depth test.

## isCull()

If `true`, the back faces are not drawn.

## getPrimitive()

Returns the topology of the draw: segments or triangles.

## isStencil()

If `true`, the key belongs to the `WRITE` pass of the stencil emulation, built by `stencil(...)`.

## equals(Object)

If `true`, the other object is of the same class with the same shader, blending and switches.

## hashCode()

Returns a hash of the values that `equals` compares.
