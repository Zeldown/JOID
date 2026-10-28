---
summary: A list of translations, rotations and scales applied to the model-view matrix around what you draw.
---

Build it once, then apply it around your drawing ([Rotate and scale a drawing](/use/learn/going-further/drawing-resources-and-transforms/#rotate-and-scale-a-drawing)).

The operations run in the order you add them, and the suppliers are read each time the transformation is applied, so a transformation can follow a value that changes.
A translation also tells the pixel grid how far the drawing moved, so that a moving node stays sharp.
A node effect transforms a node; reach for this class in your own drawing.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/).

## create()

Creates a transformation without operations.

## translate(Vector)

Adds a move by `vector`, in design pixels.

## rotate(double,Rotation)

Adds a rotation of `angle` degrees around the axis `rotation`, about the pivot given to `transform` (`(0, 0)` with `apply`).

## rotate(Supplier,Rotation)

Adds a rotation of the angle read from `angle` each time, in degrees.

## rotate(double,Rotation,Vector)

Adds a rotation of `angle` degrees around the axis `rotation`, about `pivot`; a `null` pivot uses the pivot given to `transform`.

## rotate(Supplier,Rotation,Vector)

Adds a rotation of the angle read from `angle` each time, about `pivot`.

## scale(Scale)

Adds a scale by `scale`, about the pivot given to `transform`.

## scale(Scale,Vector)

Adds a scale by `scale`, about `pivot`; a `null` pivot uses the pivot given to `transform`.

## apply()

Pushes the model-view matrix and applies the operations; call `reset()` after your drawing to pop it.

## apply(Runnable)

Applies the operations around `drawing`, then restores the model-view matrix, even when `drawing` throws.

## reset()

Pops the model-view matrix pushed by `apply()`.

## transform(double,double)

Applies the operations to the current model-view matrix without pushing it, with `pivotX`, `pivotY` as the pivot of the operations that have none.

## clear()

Removes every operation.
