---
summary: One triangle of an ObjModel, with its vertices, its optional texture coordinates and normals, and the normal of its plane.
---

Learn: [3D models](/use/learn/going-further/3d-models/).

## of(ObjVertex[],ObjTextureCoordinate[],ObjVertex[])

Creates a triangle from 3 vertices, and 3 texture coordinates and 3 normals or `null` for none; an empty array of texture coordinates also means none.
Throws `IllegalArgumentException` for any other count.
The normal of the plane is computed at once; a degenerate triangle gets `(0, 0, 1)`.

## render(Tessellator)

Adds the triangle to `tessellator`, which must be started in [[DrawMode#TRIANGLES]]: with its vertex normals when it has some, otherwise with the normal of its plane.
Texture coordinates move by 0.0005 toward the middle of the triangle, so that a texture atlas does not bleed at the edges.

## getVertices()

Returns a copy of the 3 vertices.

## getVertexNormals()

Returns a copy of the 3 normals, or `null` when the face has none.

## getTextureCoordinates()

Returns a copy of the 3 texture coordinates, or `null` when the face has none.

## getFaceNormal()

Returns the unit normal of the plane of the triangle, from the winding of its vertices.
