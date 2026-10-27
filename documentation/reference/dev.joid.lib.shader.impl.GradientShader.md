---
summary: The shader of linear gradients, which colors what is drawn along a line between two stops.
---

[[Color#bind]] and [[ColorGradient#use]] use it for a gradient color; you rarely call it yourself.

Learn: [Colors and styling](/use/learn/style-and-motion/styling/).

## inst()

Returns the shared shader.

## use(Point,Point,Color,Color,Runnable,BoundingBox)

Runs `runnable` with the gradient from `startColor` at `startPoint` to `endColor` at `endPoint`, points on 1 of `canvas`, then restores the previous shader.

## use(Point,Point,Color,Color,boolean,Runnable,BoundingBox)

Same as the other `use`; if `hasTexture` is `true`, the gradient multiplies the bound texture instead of replacing it.
