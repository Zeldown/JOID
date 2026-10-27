---
summary: A 4×4 transformation matrix with a stack that saves and restores it, as the model-view and the projection of the render bridge.
---

`getModelView()` of the render bridge, `BridgeHandler.RENDER.get()`, moves, turns and scales what you draw next; `getProjection()` maps the design canvas to the target.
The calls compose as in OpenGL: the last call applies first to what you draw.
Matrices are 16 floats in column-major order.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#use-the-matrix-stack), [Render pipeline](/build/pipeline/#matrices-and-coordinates).

## MatrixStack()

Creates a stack with the identity matrix and nothing saved.

## push()

Saves a copy of the current matrix.
Prefer `with(...)`, which restores it even when your code throws.

## pop()

Restores the matrix that the last `push()` saved.
Throws `NoSuchElementException` when nothing is saved.

## pop(int)

Restores saved matrices until `depth` of them remain; nothing happens when the stack holds `depth` or fewer.

## with(Consumer)

Saves the matrix, runs `body` with the stack, then restores the matrix, even when `body` throws.

## identity()

Replaces the current matrix with the identity: no transformation.

## translate(double,double,double)

Moves what you draw next by `x`, `y` and `z`, in design pixels in a UI.

## scale(double,double,double)

Scales what you draw next by `x`, `y` and `z` around the current origin; a negative factor mirrors.

## rotate(double,double,double,double)

Turns what you draw next by `angle` degrees around the axis `x`, `y`, `z` through the current origin.
In a UI, the axis `0D, 0D, 1D` turns in the plane of the screen; a zero axis does nothing.

## ortho(double,double,double,double,double,double)

Replaces the current matrix with an orthographic projection that maps `left` to `right` and `bottom` to `top` onto the target, between the depths `near` and `far`.
A `bottom` larger than `top` puts the origin at the top-left corner, as the projection of a UI does.

## multiply(float[])

Multiplies the current matrix by 16 values in column-major order, applied before the current matrix.

## load(float[])

Replaces the current matrix with a copy of 16 values in column-major order.

## getDepth()

Returns the number of saved matrices.

## getMatrix()

Returns a copy of the current matrix.

## getRawMatrix()

Returns the current matrix itself, without a copy, for a backend that reads it at every draw.
Never change the array: the next call of the stack can replace it or change it in place.

## getNormalMatrix()

Returns the 3×3 matrix, column-major, that transforms normals for the current matrix: the inverse transpose of its rotation and scale part, all zeros when that part cannot be inverted.
Shaders receive it as `uNormalMatrix`.
