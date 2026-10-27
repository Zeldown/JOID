---
summary: Rounds the corners of what a node draws, all four or only some of them.
---

The corners are cut from the pixels of the node, so it rounds a rectangle, an image or a video alike.
The radius never goes past half the shorter side: `create(999F)` turns a rectangle into a pill.
A [[BorderNodeEffect]] on the same node outlines the rounded shape, and a [[ShadowNodeEffect]] takes its radius and its sides.
Its priority is `100`, so its pass runs before a blur and a border.

Learn: [Effects](/use/learn/style-and-motion/effects/).

## create(float)

Creates an effect that rounds the four corners with this radius, in design pixels.

## create(Supplier)

Creates an effect whose radius follows a `Supplier`: a signal, a derived value or a lambda.

## RoundedNodeEffect(Supplier)

Called by a subclass with the same argument as `create(Supplier)`.

## radius

Radius of the corners, in design pixels, bounded by half the shorter side of the node.

## top

If `false`, the two top corners stay square: a corner is rounded only when both of its sides are.

## bottom

If `false`, the two bottom corners stay square.

## left

If `false`, the two left corners stay square.

## right

If `false`, the two right corners stay square.

## toShaderPass(Node)

Returns the pass that cuts the corners, with the edges of the node snapped to the screen pixels.
