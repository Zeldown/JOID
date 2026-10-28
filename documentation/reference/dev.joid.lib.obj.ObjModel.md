---
summary: A 3D model read from a Wavefront .obj file, drawn with one texture.
---

Load it once and keep it in a field ([Load a model](/use/learn/going-further/3d-models/#load-a-model)); a model node or [[DrawModel]] draws it.
The file is read at once, on the calling thread ([Know what JOID reads in a .obj file](/use/learn/going-further/3d-models/#know-what-joid-reads-in-a-obj-file)).

Learn: [3D models](/use/learn/going-further/3d-models/).

## of(String,Object,Resource)

Reads the model `name` from anything [[Asset#of]] accepts, as UTF-8 text, drawn with `texture`.
Throws `IllegalArgumentException` that names the line when the file is malformed: a line that does not parse, an index out of range, or a group that mixes faces with and without texture coordinates.
Throws `UncheckedIOException` when the file cannot be read.

## render()

Draws every group with the texture bound and repeated, in model units.
JOID calls it from [[DrawModel#drawModel]].

## getDepth()

Returns the extent of the vertices along Z, in model units.

## getWidth()

Returns the extent of the vertices along X, in model units.

## getHeight()

Returns the extent of the vertices along Y, in model units.

## getVertices()

Returns every vertex of the file, in order, as a list you cannot change.

## getVertexNormals()

Returns every normal of the file, in order, as a list you cannot change.

## getTextureCoordinates()

Returns every texture coordinate of the file, in order, with V flipped so that `0` is the top of the texture, as a list you cannot change.

## getGroups()

Returns the groups, in order, as a list you cannot change; faces before the first `g` or `o` line form the group `Default`.

## getName()

Returns the name given to `of`, shown in the error messages.

## getTexture()

Returns the texture the model draws with.
