---
summary: Draws a soft shadow of the shape of a node under it, or a glow when the color is light.
---

The shadow does not read the pixels of the node: it draws a rounded rectangle with the radius and the sides of the [[RoundedNodeEffect]] of the node, a disc for a [[CircleNode]] or a node with a [[CircleNodeEffect]], and a plain rectangle otherwise.
It is drawn before the node and its children, which cover it.
Its priority is `-50` ([Control the order of effects](/use/learn/style-and-motion/effects/#control-the-order-of-effects)).

Learn: [Effects](/use/learn/style-and-motion/effects/).

## create(Color,float)

Creates a shadow of this color and blur, in design pixels, centered under the node.

## create(Supplier,Supplier)

Creates a shadow whose color and blur follow suppliers: signals, derived values or lambdas.

## ShadowNodeEffect(Supplier,Supplier)

Called by a subclass with the same arguments as `create(Supplier,Supplier)`.

## blur

Softness of the shadow, in design pixels: the distance over which it fades out around the shape.

## color

Color of the shadow where it is densest; its alpha sets the strength.

## offsetX

Horizontal shift of the shadow, in design pixels; a positive value moves it to the right.

## offsetY

Vertical shift of the shadow, in design pixels; a positive value moves it down.

## draw(Node,Runnable)

Draws the shadow, then the node.
