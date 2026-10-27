---
summary: Moves, rotates or scales the drawing of a node and its children without changing the layout.
---

The effect applies a [[Transformation]] to the model-view matrix while the node and its children draw.
A rotation or a scale without a pivot of its own turns or scales around the center of the node.
Its priority is `-100`, the lowest of the built-in effects, so it moves the shadow, the mask and the pixel effects with the node.
The node still receives the mouse at its layout position.

Learn: [Effects](/use/learn/style-and-motion/effects/), [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#rotate-and-scale-a-drawing).

## create(Transformation)

Creates an effect that applies the operations of this transformation, in their order.

## TransformNodeEffect(Transformation)

Called by a subclass with the same argument as `create`.

## transformation

Operations applied to the drawing of the node.
A rotation that reads a `Supplier` for its angle follows it, so an animation turns the node without replacing the transformation.

## draw(Node,Runnable)

Saves the model-view matrix, applies the transformation around the center of the node, draws the node, then restores the matrix, even when the drawing throws.
