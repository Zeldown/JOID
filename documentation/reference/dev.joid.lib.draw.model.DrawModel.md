---
summary: Draws a 3D model into the UI, scaled to a size in design pixels, with the depth test and the lighting on.
---

Reach it through `DrawUtils.MODEL`.
The model draws inside its own render state: depth test and depth write on, lighting on, backface culling off; the depth buffer is cleared before and after, so the model never hides behind or in front of the rest of the UI.
The Y axis of the model points up: JOID flips it to match the design canvas.

Learn: [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/), [3D models](/use/learn/going-further/3d-models/).

## inst()

Returns the shared instance, the one in `DrawUtils.MODEL`.

## drawModel(double,double,double,IDrawableModel)

Draws the model with its origin at `x`, `y`, scaled by `size` on every axis: one unit of the model becomes `size` design pixels.

## drawModel(double,double,double,double,double,IDrawableModel)

Draws the model with its origin at `x`, `y`, scaled by `sizeX`, `sizeY` and `sizeZ` on each axis.
