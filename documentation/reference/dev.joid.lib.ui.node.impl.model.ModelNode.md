---
summary: Draws a 3D model at a zoom and a rotation that your code sets.
---
Learn: [3D models](/use/learn/going-further/3d-models/#show-and-turn-a-model).

## create(double,double,double,double)

Creates a model node at `x`, `y` with this width and height.

## ModelNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## zoom

Scale of the model: `1` fits its width to the node.

## rotationYaw

Rotation of the model around its vertical axis, in degrees.

## rotationPitch

Rotation of the model around its horizontal axis, in degrees.

## draw(double,double)

Draws the model at its zoom and rotation.
