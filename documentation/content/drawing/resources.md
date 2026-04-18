# Resources

`DrawResource` draws an already-loaded `Resource` (image or video) to the screen. It is the low-level counterpart to `ResourceNode` — same blend mode setup, same texture coordinates, but you choose when and where to draw. Reach it through `DrawUtils.RESOURCE` or directly via `DrawResource.getInstance()` — both return the same singleton. The resource must already be prepared (typically by binding it through a node first, or triggering a `ResourceBuilder.of(...)` upstream).

## Sizing

### `drawResource` (natural size)

```java
void drawResource(double x, double y, Resource resource)
```

Draws the resource at its intrinsic width and height. The top-left corner is `(x, y)`.

```java
DrawUtils.RESOURCE.drawResource(40, 40, logo);
```

### `drawResource` (explicit size)

```java
void drawResource(double x, double y, double width, double height, Resource resource)
```

Stretches the resource to fill the given rectangle. Aspect ratio is **not** preserved.

```java
DrawUtils.RESOURCE.drawResource(0, 0, 1920, 1080, background);
```

## Aspect-preserving

### `drawScaledResourceWidth`

```java
void drawScaledResourceWidth(double x, double y, double width, Resource resource)
```

Computes `height = width × (resource.height / resource.width)` — aspect ratio preserved, only the width is specified.

```java
DrawUtils.RESOURCE.drawScaledResourceWidth(20, 20, 400, photo);
```

### `drawScaledResourceHeight`

```java
void drawScaledResourceHeight(double x, double y, double height, Resource resource)
```

Symmetric to the above: `width = height × (resource.width / resource.height)`.

```java
DrawUtils.RESOURCE.drawScaledResourceHeight(20, 20, 200, photo);
```

### `drawCenteredResource`

```java
void drawCenteredResource(double x, double y, double width, double height, Resource resource)
```

Scales the resource to fit inside `(x, y, width, height)` while preserving aspect ratio, and centers the result. Equivalent to CSS `object-fit: contain`.

```java
DrawUtils.RESOURCE.drawCenteredResource(0, 0, 400, 400, thumbnail);
```

## Texture-coord override

If the `Resource`'s `ResourceProperties` has custom `textureCoords` (set via `ResourceBuilder.textureCoords(u, v, w, h)`), every `drawResource` call honours it — only the sub-rectangle is sampled. Useful for sprite sheets.

```java
Resource sprite = ResourceBuilder.create()
    .textureCoords(0, 0, 64, 64)
    .of(spriteSheet);
```

## GL state

Every draw call pushes the matrix, enables `GL_BLEND` with `(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)` and `GL_POINT_SMOOTH`, binds the resource texture with `GL_CLAMP` wrap on both axes, draws a textured quad, and restores the state. You don't need to preconfigure blending.

## See also

- `ResourceBuilder` — how to load and configure resources.
- `ResourceNode` — node-level wrapper with hover/click integration.
- `Decoders` — image and video decoders.