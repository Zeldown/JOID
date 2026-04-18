# ModelNode

Renders a 3D OBJ model inside the UI. Useful for preview viewers, character selectors, item showcases.

## Create

```java
ModelNode.create(x, y, width, height)
    .model(OBJModel.load(name, getClass().getResourceAsStream("/mesh.obj"), texture))
    .attach(parent);
```

## API

```java
node.model(OBJModel);
node.rotation(float x, float y, float z);     // radians
node.rotationSpeed(float x, float y, float z); // auto-rotate (rad/s)
node.scale(float);
node.translate(float x, float y, float z);
```

## Interactive viewer

Use `ModelViewerNode` for built-in mouse drag rotation and scroll zoom:

```java
ModelViewerNode.create(0, 0, 400, 400)
    .model(model)
    .attach(parent);
```

## OBJ loading

```java
final Resource texture = Resource.of(getClass().getResourceAsStream("/texture.png"));
final OBJModel model = OBJModel.load("myMesh", getClass().getResourceAsStream("/mesh.obj"), texture);
```

Supports standard OBJ with vertex normals, UVs, and faces. MTL files aren't parsed — textures are applied from the `Resource` parameter.

## Best practices

- **Pre-load models.** OBJ parsing isn't free; do it at startup, not in `init()`.
- **Use `ModelViewerNode` for user-facing previews.** Wiring drag/zoom manually is tedious.
- **Keep poly counts low.** JOID uses immediate-mode rendering — thousands of triangles are fine, hundreds of thousands will tank framerate.

## See also

- [ResourceBuilder](../../resources/resource-builder.md) — texture loading.
