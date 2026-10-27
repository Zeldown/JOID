---
summary: Hides every pixel of a node and its children outside a rectangle, or outside the opaque part of an image.
---

The rectangle starts at the top-left corner of the node and, without a size, covers the bounds of the node: `MaskNodeEffect.create()` alone hides what the children draw outside the node.
Masks nest: a masked node inside another mask shows only what both keep.
Its priority is `0`, so a [[TransformNodeEffect]], at `-100`, turns the mask with the node; a priority under `-100` keeps the mask in place while the node turns under it.
The mask clips the drawing only: the hidden part of the node still receives the mouse.

Learn: [Effects](/use/learn/style-and-motion/effects/), [Clip your own drawing](/use/learn/style-and-motion/effects/#clip-your-own-drawing).

## create()

Creates a mask that covers the whole node; the setters place and size it.

## MaskNodeEffect()

Called by a subclass.

## x

Horizontal offset of the visible rectangle from the left edge of the node, in design pixels.

## y

Vertical offset of the visible rectangle from the top edge of the node, in design pixels.

## width

Width of the visible rectangle, in design pixels; without a value, the width of the node.
`getWidth()` returns the width that the mask used on the last frame, `0D` before the node is drawn.

Default: the width of the node, read on every frame

## height

Height of the visible rectangle, in design pixels; without a value, the height of the node.
`getHeight()` returns the height that the mask used on the last frame, `0D` before the node is drawn.

Default: the height of the node, read on every frame

## resource

[[Resource]] stretched over the visible rectangle, whose opaque part forms the mask: it keeps the pixels where the image has an alpha above `0.5F`, so the edges are hard.
`null` masks with the plain rectangle.

## draw(Node,Runnable)

Draws the node and its children through [[UI#mask]], with the rectangle or the image of the effect.
