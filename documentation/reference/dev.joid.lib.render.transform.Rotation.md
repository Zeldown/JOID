---
summary: The axis of a rotation in a `Transformation`, as yaw, pitch and roll components.
---

`ROLL` turns the drawing in the plane of the screen; `YAW` and `PITCH` turn it in depth.
The components are read each time the rotation is applied.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/).

## create(double,double,double)

Creates the axis with the components `yaw` (Y axis), `pitch` (X axis) and `roll` (Z axis).

## create(Supplier,Supplier,Supplier)

Creates an axis whose components are read from the suppliers each time it is applied.

## getYaw()

Returns the component along the vertical axis.

## getRoll()

Returns the component along the axis that points out of the screen.

## getPitch()

Returns the component along the horizontal axis.

## getYawSupplier()

Returns the supplier of the yaw component.

## getPitchSupplier()

Returns the supplier of the pitch component.

## getRollSupplier()

Returns the supplier of the roll component.

## YAW

Turns around the vertical axis.

## ROLL

Turns in the plane of the screen, around the axis that points out of it.

## PITCH

Turns around the horizontal axis.
