---
summary: A texture coordinate of an ObjModel, on 1 of the texture.
---

Learn: [3D models](/use/learn/going-further/3d-models/).

## of(float,float)

Creates the coordinate `u`, `v`, with `w` at `0F`.

## of(float,float,float)

Creates the coordinate `u`, `v`, `w`.

## getU()

Returns the horizontal coordinate, `0F` at the left of the texture.

## getV()

Returns the vertical coordinate; [[ObjModel]] flips the `vt` value of the file so that `0F` is the top of the texture.

## getW()

Returns the depth coordinate of a 3D texture, read from the file and unused when drawing.
