---
summary: The two stops and the direction of a linear gradient, carried by a gradient `Color`.
---

You rarely build one yourself: [[Color#toGradient]] and [[Color#decode]] with `gradient(...)` create the gradient color for you.
The points are on 1 of the drawn area, so the same gradient fits a node of any size.

Learn: [Colors and styling](/use/learn/style-and-motion/styling/).

## of(Color,Color,Point,Point)

Creates a gradient from `startColor` at `startPoint` to `endColor` at `endPoint`, both points on 1 of the drawn area (`(0, 0)` to `(1, 0)` runs from left to right).

## use(Runnable,BoundingBox)

Runs `runnable` with the gradient shader bound and spread over `canvas`, in the coordinates you draw in, then restores the previous shader.

## use(boolean,Runnable,BoundingBox)

Runs `runnable` with the gradient shader bound over `canvas`, then restores the previous shader; if `hasTexture` is `true`, the gradient multiplies the bound texture instead of replacing it.

## getStartColor()

Returns the color at the start point and before it.

## getEndColor()

Returns the color at the end point and after it.

## getStartPoint()

Returns the start point, on 1 of the drawn area.

## getEndPoint()

Returns the end point, on 1 of the drawn area.

## equals(Object)

If `true`, the other object is of the same class with the same colors and points.

## hashCode()

Returns a hash of the values that `equals` compares.
