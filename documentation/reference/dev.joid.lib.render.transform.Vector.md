---
summary: A 3D offset or pivot of a Transformation, in design pixels, fixed or read from suppliers.
---

Each coordinate is read each time the transformation is applied.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/).

## create()

Creates the vector `(0, 0, 0)`.

## create(double,double)

Creates the vector `x`, `y`, with `z` at `0D`.

## create(double,double,double)

Creates the vector `x`, `y`, `z`.

## create(Supplier,Supplier)

Creates a vector whose `x` and `y` are read from the suppliers, with `z` at `0D`.

## create(Supplier,Supplier,Supplier)

Creates a vector whose coordinates are read from the suppliers.

## x

The X coordinate: `getX()` reads it, `x(double)` fixes it, `x(Supplier)` reads it from a supplier.

## y

The Y coordinate, down: `getY()` reads it, `y(double)` fixes it, `y(Supplier)` reads it from a supplier.

## z

The Z coordinate: `getZ()` reads it, `z(double)` fixes it, `z(Supplier)` reads it from a supplier.

## add(double,double,double)

Adds `x`, `y`, `z` to the coordinates; a coordinate read from a supplier keeps following it, plus the offset.

## add(Supplier,Supplier,Supplier)

Adds the values of the suppliers to the coordinates, read each time.

## getXSupplier()

Returns the supplier of the X coordinate.

## getYSupplier()

Returns the supplier of the Y coordinate.

## getZSupplier()

Returns the supplier of the Z coordinate.
