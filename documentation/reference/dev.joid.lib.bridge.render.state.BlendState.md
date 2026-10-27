---
summary: How a draw combines its colors with the pixels already on the target, an equation and four factors.
---

`BlendState` is immutable and compares by value: keep your own blend states in constants, and pass one to `blend(...)` of the [[RenderState]].
The calls of `DrawUtils` set the blending they need; a blend of yours applies to the vertices you send with the [[Tessellator]].

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#blend-with-what-is-under), [The render state](/build/pipeline/#the-render-state).

## of(Equation,Factor,Factor)

Creates an enabled blend state with the same source and destination factors for the color and for the alpha.

## of(Equation,Factor,Factor,Factor,Factor)

Creates an enabled blend state with separate factors: source and destination for the color channels, then source and destination for the alpha.

## NORMAL

The usual transparency: the color covers what is under it in proportion to its alpha, and the alpha of the target adds up.
The drawing of JOID uses it for shapes, text and images.

## DISABLED

No blending: the color replaces the pixel, alpha included.
It is the blending of a fresh render state.

## PREMULTIPLIED

Blending for colors already multiplied by their alpha, such as the content of a framebuffer drawn over the screen.

## isEnabled()

If `false`, the backend turns blending off; only `DISABLED` returns `false`.

## getEquation()

Returns the operation that combines the weighted source and destination.

## getSourceColor()

Returns the factor applied to the RGB channels of the color being drawn.

## getDestinationColor()

Returns the factor applied to the RGB channels already on the target.

## getSourceAlpha()

Returns the factor applied to the alpha of the color being drawn.

## getDestinationAlpha()

Returns the factor applied to the alpha already on the target.
