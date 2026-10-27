---
summary: Base class of the nodes that draw a 3D model in their bounds.
---
The model is scaled so that its width fills the width of the node, centered.

Learn: [3D models](/use/learn/going-further/3d-models/).

## BaseModelNode(double,double,double,double)

Called by a subclass with the position and the size of the node.

## model

The model to draw.
Throws an `IllegalStateException` when the node is attached without one.

## drawModel(double,double,double)

Draws the model at this zoom and rotation, in degrees, around its center; a zoom of `0` draws nothing.
