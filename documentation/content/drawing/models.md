# 3D Models

`DrawModel` (`dev.joid.lib.draw.model`) draws a 3D model in the UI through `DrawUtils.MODEL`. A model is anything that implements `IDrawableModel`; JOID ships `OBJModel`, which reads Wavefront `.obj` files with one texture. To show a model as a node, with its size fitted to the node, use `ModelNode` (see [ModelNode and ModelViewerNode](../nodes/visual/model.md)).

## Quick example

```java
final Resource texture = Resource.of(UIShop.class.getResourceAsStream("/assets/models/crate.png"));
final OBJModel crate = OBJModel.load("crate.obj", UIShop.class.getResourceAsStream("/assets/models/crate.obj"), texture);

ModelNode.create(0, 0, 300, 300).model(crate).attach(flex);
```

In a draw hook, draw it directly:

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.MODEL.drawModel(960D, 540D, 120D, this.crate);
}
```

## DrawModel reference

| Method | Description |
|---|---|
| `drawModel(double x, double y, double size, IDrawableModel model)` | Draws the model with its origin on `(x, y)`, one model unit = `size` UI units on every axis. |
| `drawModel(double x, double y, double sizeX, double sizeY, double sizeZ, IDrawableModel model)` | Same, with a scale per axis. |
| `DrawModel.getInstance()` | The instance behind `DrawUtils.MODEL`. |

Around `model.render()`, `drawModel`:

1. pushes the matrix, translates to `(x, y, 0)`, scales by `(sizeX, sizeY, sizeZ)` and rotates 180° around the Y axis;
2. turns face culling off and lighting on;
3. renders the model;
4. turns lighting off and pops the matrix, even when `render()` throws.

| Model axis | On screen |
|---|---|
| +X | Toward the left: model point `(1, 0, 0)` lands on `x - sizeX`. |
| +Y | Down, like the UI canvas: model point `(0, 1, 0)` lands on `y + sizeY`. |
| +Z | Mirrored by the rotation, like X. |

- Face culling stays off after the call: wrap it in `pushState()`/`popState()` if you rely on culling (see [Render state basics](draw-utils.md#render-state-basics)).
- The depth test is not enabled: the triangles are drawn in the order of the model.
- Lighting gives each face a flat shade computed from its normal by the default shader of the backend.

## OBJModel

`OBJModel` (`dev.joid.lib.obj`) parses an `.obj` file into vertices, normals, texture coordinates and groups of faces, and draws them with one texture.

| Member | Description |
|---|---|
| `OBJModel.load(String name, InputStream stream, Resource texture)` | Reads the whole stream and closes it, even on failure. `name` appears in the error messages. |
| `render()` | Binds the texture (repeated outside 0–1), draws every group, then unbinds it. |
| `getWidth()`, `getHeight()`, `getDepth()` | Size of the bounding box of the vertices, in model units; `0` without vertex. |
| `getVertices()`, `getVertexNormals()`, `getTextureCoordinates()`, `getGroups()` | The parsed data. |
| `getName()`, `setName(String)`, `getTexture()`, `setTexture(Resource)` | Name and texture; change the texture at any time. |
| `new OBJModel()` | Empty model to fill by hand through the lists above; set a texture before rendering it. |

### Supported OBJ statements

| Statement | Read as |
|---|---|
| `v x y z` | A vertex. A fourth value (weight) is accepted and ignored. |
| `vn x y z` | A vertex normal. |
| `vt u v` or `vt u v w` | A texture coordinate; `v` is flipped (`1 - v`) to match the texture orientation. |
| `f` with 3 or 4 corners | A triangle or a quad, written `v`, `v/vt`, `v//vn` or `v/vt/vn`, with 1-based indices. |
| `g name` or `o name` | Starts a new group. Names use letters, digits, `_` and `.`, several names separated by spaces are accepted. |
| `#` comments, blank lines | Skipped. |
| Anything else (`mtllib`, `usemtl`, `s`, `l`...) | Ignored. |

Whitespace is collapsed, so tabs and repeated spaces are accepted. Faces before any `g`/`o` go to a group named `Default`, and a file without any face and without `g`/`o` has no group.

### Limits of the OBJ parser

A line that breaks one of these rules throws a `RuntimeException` naming the file and the line, for example `Error parsing entry ('v 1 2', line 2) in file 'crate.obj' - Incorrect format`:

- Numbers are plain decimals (`-1`, `2`, `0.5`): no exponent, no leading `+`, no leading `.`.
- Face indices are positive; negative (relative) indices are not supported. An index past the declared elements throws an `IndexOutOfBoundsException`.
- A face has 3 or 4 corners, all written in the same format.
- A group holds only triangles or only quads: triangulate the model, or split triangles and quads into separate groups.
- A group name with another character, such as `-`, is refused.

A read failure of the stream is rethrown as a `RuntimeException` whose cause is the `IOException`. Materials are not read: the model uses the texture you pass, which loads like any `Resource`; until it is loaded, the model is drawn untextured in the current color.

### OBJ data classes

The parsed data lives in `dev.joid.lib.obj.data`:

| Class | Members |
|---|---|
| `OBJGroup` | `getName()`, `getDrawMode()` (`TRIANGLES` or `QUADS`), `getFaces()` and their setters; constructors `OBJGroup()`, `OBJGroup(String name)`, `OBJGroup(String name, DrawMode drawMode)`; `render()` draws its faces in one call. |
| `OBJFace` | `getVertices()`, `getTextureCoordinates()`, `getVertexNormals()`, `getFaceNormal()` and their setters; `normal()` computes the normal from the first three vertices; `render(Tessellator tessellator)` adds the face to a started tessellator. |
| `OBJVertex` | `getX()`, `getY()`, `getZ()`; constructors `OBJVertex(float x, float y, float z)` and `OBJVertex(float x, float y)` (z = 0). |
| `OBJTextureCoordinate` | `getU()`, `getV()`, `getW()`; constructors `OBJTextureCoordinate(float u, float v, float w)` and `OBJTextureCoordinate(float u, float v)` (w = 0). |

A face is shaded with its face normal, `(v1 - v0) × (v2 - v0)` normalized, so it follows the winding of the face. The vertex normals are read but not used for shading. Each texture coordinate is pulled 0.0005 toward the center of the face's coordinates, which keeps neighboring texels of an atlas from bleeding on the edges.

## Writing a model with IDrawableModel

`IDrawableModel` (`dev.joid.lib.draw.model.utils`) is what `DrawModel` and `ModelNode` draw:

| Method | Description |
|---|---|
| `render()` | Emits the geometry, in model units, inside the transform of `drawModel`. |
| `getWidth()`, `getHeight()`, `getDepth()` | Size of the model, in model units. `ModelNode` fits the model to its width with them. |

```java
public final class TriangleModel implements IDrawableModel {

    @Override
    public void render() {
        final Tessellator tessellator = Tessellator.inst();
        tessellator.start(DrawMode.TRIANGLES);
        tessellator.setColor(255, 180, 0);
        tessellator.setNormal(0F, 0F, 1F);
        tessellator.addVertex(-0.5D, 0.5D, 0D);
        tessellator.addVertex(0.5D, 0.5D, 0D);
        tessellator.addVertex(0D, -0.5D, 0D);
        tessellator.draw();
    }

    @Override
    public double getDepth() {
        return 0D;
    }

    @Override
    public double getWidth() {
        return 1D;
    }

    @Override
    public double getHeight() {
        return 1D;
    }

}
```

The `Tessellator` is described on [Transformations and Framebuffers](transformations.md#building-geometry-with-tessellator).

## See also

- [ModelNode and ModelViewerNode](../nodes/visual/model.md)
- [Resources](../resources/resources.md)
- [Transformations and Framebuffers](transformations.md)
- [Drawing Overview](draw-utils.md)