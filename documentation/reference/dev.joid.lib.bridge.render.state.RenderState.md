---
summary: The settings that every draw reads, color, blending, texture, shader, lines, depth, stencil, viewport and target.
---

`getState()` of the render bridge, `BridgeHandler.RENDER.get()`, holds the current state.
Change it inside `withState(...)` of the render bridge, which restores it afterwards, even when your code throws; the setters return the state, so they chain.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#change-the-render-state), [The render state](/build/pipeline/#the-render-state).

## create()

Creates a state with the defaults: opaque white, no blending, no texture, no shader, the window as target, and depth and color writes on.

## cull

If `true`, the backend skips the triangles that face away from the viewer.

## lighting

If `true`, the default shader shades each vertex by its normal, with a light that faces the viewer, as for 3D models.

## lineWidth

Width of the lines that the [[Tessellator]] draws, in design pixels like every other thickness: `2F` covers 4 screen pixels at a scale of 2.
A width other than `1F`, or a smooth line, draws each segment as a quad; a line of `1F` without `lineSmooth` stays the line of one screen pixel of the GPU.

## depthTest

If `true`, a fragment farther than the depth already stored is discarded.

## depthWrite

If `true`, the draws write their depth into the depth buffer.

## lineSmooth

If `true`, the lines get antialiased edges.

## alphaCutoff

Alpha, on 1, at or below which the shaders discard a fragment; `0F` keeps every fragment.
[[UI#mask]] sets `0.5F` to keep only the opaque part of an image.

## blend

How the next draws combine with the pixels already on the target, a [[BlendState]].

## stencil

Stencil test and operations of the next draws, a [[StencilState]].
It works on the window only: a framebuffer has no stencil.

## shader

Shader of the next draws; `null` draws with the default shader of the backend, which draws the vertex colors and the texture.
A [[ShaderProgram]] sets it while it is bound.

## texture

Texture that the next draws sample; `null` draws plain colors.
Setting a texture turns `textureMipmap` back on.

## frameBuffer

Target of the next draws; `null` is the window.

## textureMipmap

If `false`, the next draws sample the texture without its mip levels, even when it has some.

## textureWrap

What the texture shows outside the coordinates 0 to 1, a [[TextureWrap]].

## textureFilter

How the texture is sampled between its pixels, a [[TextureFilter]].

## colorWrite

If `false`, the draws write nothing into the color channels, only depth or stencil, as the masks do.

## color(float,float,float,float)

Sets the current color, channels on 1: the color of the vertices that carry none, so it tints a textured draw without vertex colors.

## viewport(int,int,int,int)

Sets the rectangle of the target that the projection maps to, in pixels: `x`, `y` from the bottom-left corner, `width`, `height`.
A draw with an empty viewport draws nothing.

## copy()

Returns an independent copy of the whole state.

## load(RenderState)

Copies every setting of another state into this one.

## getRed()

Returns the red channel of the current color, on 1.

## getBlue()

Returns the blue channel of the current color, on 1.

## getGreen()

Returns the green channel of the current color, on 1.

## getAlpha()

Returns the alpha channel of the current color, on 1.

## getViewportX()

Returns the left edge of the viewport, in pixels of the target.

## getViewportY()

Returns the bottom edge of the viewport, in pixels of the target counted from its bottom row.

## getViewportWidth()

Returns the width of the viewport, in pixels.

## getViewportHeight()

Returns the height of the viewport, in pixels.
