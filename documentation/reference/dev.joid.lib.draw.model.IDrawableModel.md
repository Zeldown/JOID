---
summary: A 3D model that JOID can draw and size, which renders its geometry and reports its extent.
---

[[ObjModel]] implements it for Wavefront `.obj` files; implement it for another source of geometry.
A model node reads the extent to fit the model in its bounds.

Learn: [3D models](/use/learn/going-further/3d-models/).

## render()

JOID calls it inside [[DrawModel#drawModel]], with the matrices and the 3D render state already set.
Bind your texture and draw your geometry in model units, for example with the [[Tessellator]].

## getDepth()

Returns the extent of the model along Z, in model units.

## getWidth()

Returns the extent of the model along X, in model units.

## getHeight()

Returns the extent of the model along Y, in model units.
