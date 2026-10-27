---
summary: Outlines the pixels that a node draws, outside or inside their edge.
---

The outline follows the drawn shape, not the bounds: rounded corners, a disc, glyphs, the opaque part of an image.
A gradient [[Color]] runs across the bounds of the node.
Its priority is `200`, so it outlines the result of the rounded, circle and blur passes, whatever the order you add them in.
The border setters of a [[RectNode]] drive the `BorderNodeEffect` of the node: give a rectangle one or the other.

Learn: [Effects](/use/learn/style-and-motion/effects/).

## create(Color,float)

Creates a border of this color and width, in design pixels, drawn outside the shape.

## BorderNodeEffect(Supplier,Supplier)

Called by a subclass with suppliers of the color and of the width.

## color

Color of the border.
The overload that takes a `Function` receives the node, so that the color can depend on its state, such as its focus.

## width

Thickness of the border, in design pixels; `0F` or less turns the effect off.

## mode

Side of the edge where the border lies: `BorderMode.OUT` around the shape, `BorderMode.IN` over its outer pixels.

## corners

If `false`, an `OUT` border leaves the corners of the node open and runs along its four sides only.

## getColor(Node)

Returns the color of the border for this node, as the `Function` overload of `color` computes it.

## shouldApply(Node)

Returns `false` while the width is `0F` or less.

## toShaderPass(Node)

Returns a [[BorderShaderPass]] with the current color, width, mode and corners.
