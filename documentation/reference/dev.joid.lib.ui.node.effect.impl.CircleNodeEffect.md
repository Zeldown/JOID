---
summary: Cuts what a node draws to the largest disc centered in its bounds.
---

The disc takes the shorter side of the node as its diameter: a square image becomes a round avatar.
A [[ShadowNodeEffect]] on the same node draws a round shadow, and a [[BorderNodeEffect]] outlines the disc.
Its priority is `100`, so its pass runs before a blur and a border.

Learn: [Effects](/use/learn/style-and-motion/effects/).

## create()

Creates the effect.

## CircleNodeEffect()

Called by a subclass.

## toShaderPass(Node)

Returns the pass that keeps the disc centered in the node, of radius half its shorter side.
