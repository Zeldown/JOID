---
summary: The factors of a scale in a `Transformation`, one per axis, fixed or read from a supplier.
---

`1D` keeps the size, `2D` doubles it, a negative factor mirrors the drawing.
Each factor is read each time the scale is applied.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/).

## create()

Creates a scale that keeps the size on every axis.

## create(double,double,double)

Creates a scale by `width`, `height` and `depth` along X, Y and Z.

## create(Supplier,Supplier,Supplier)

Creates a scale whose factors are read from the suppliers each time it is applied.

## width

The factor along X: `getWidth()` reads it, `width(double)` fixes it, `width(Supplier)` reads it from a supplier.

## height

The factor along Y: `getHeight()` reads it, `height(double)` fixes it, `height(Supplier)` reads it from a supplier.

## depth

The factor along Z: `getDepth()` reads it, `depth(double)` fixes it, `depth(Supplier)` reads it from a supplier.

## getWidthSupplier()

Returns the supplier of the X factor.

## getHeightSupplier()

Returns the supplier of the Y factor.

## getDepthSupplier()

Returns the supplier of the Z factor.
