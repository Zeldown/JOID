---
summary: A named group of triangles of an `ObjModel`, drawn in one batch.
---

Learn: [3D models](/use/learn/going-further/3d-models/).

## create(String)

Creates an empty group named `name`.

## face(ObjFace)

Adds a triangle to the group.

## render()

Draws every triangle of the group in one [[Tessellator]] batch, with the texture and the render state already set.

## getFaces()

Returns the triangles, in order, as a list you cannot change.

## getName()

Returns the name of the group: the rest of its `g` or `o` line.
