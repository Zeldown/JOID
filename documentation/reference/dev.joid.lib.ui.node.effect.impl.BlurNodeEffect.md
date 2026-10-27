---
summary: Blurs what a node draws with a horizontal then a vertical Gaussian pass.
---

It blurs the pixels of the node, and with the `CHILDREN` scope those of its children too; it never blurs what lies behind the node.
Its priority is `150`: it blurs the result of a rounded or circle pass, and a border drawn after it stays sharp.

Learn: [Effects](/use/learn/style-and-motion/effects/).

## create(float)

Creates a blur of this radius, in design pixels.

## create(Supplier)

Creates a blur whose radius follows a `Supplier`: a signal, a derived value or a lambda.

## BlurNodeEffect(Supplier)

Called by a subclass with the same argument as `create(Supplier)`.

## radius

Strength of the blur, in design pixels; `0F` or less turns the effect off.

## shouldApply(Node)

Returns `false` while the radius is `0F` or less.

## toShaderPasses(Node)

Returns a horizontal and a vertical [[BlurShaderPass]] of the current radius.
