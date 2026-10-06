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

Stretches the resource to fill the given rectangle. Aspect ratio is **not** preserved: to keep it, draw through a `ResourceNode` or a `ResourcePlayerNode` with `StretchType.CONTAIN` or `StretchType.COVER`.

```java
DrawUtils.RESOURCE.drawResource(0, 0, 1920, 1080, background);
```

### `drawResource` (region)

```java
void drawResource(double x, double y, double width, double height, double u, double v, double regionWidth, double regionHeight, Resource resource)
```

Draws only the region `(u, v, regionWidth, regionHeight)` of the image, in pixels of the image, stretched to the rectangle. `StretchType.COVER` uses it to crop the centered part of a resource to its node.

```java
DrawUtils.RESOURCE.drawResource(0, 0, 200, 200, 100, 0, 200, 200, banner);
```

## Texture-coord override

If the `Resource`'s `ResourceProperties` has custom `textureCoords` (set via `ResourceBuilder.textureCoords(u, v, width, height)`, in pixels of the image), every `drawResource` call honours it — only the sub-rectangle is sampled. `drawResource(x, y, resource)` draws it at its own size, and an explicit size stretches it. Useful for sprite sheets.

```java
Resource sprite = ResourceBuilder.create()
    .textureCoords(0, 0, 64, 64)
    .of(spriteSheet);
```

## Render state

Every draw call pushes the matrix, enables `BlendState.NORMAL` (`SRC_ALPHA`, `ONE_MINUS_SRC_ALPHA`), binds the resource texture with `TextureWrap.CLAMP_TO_EDGE` — `CLAMP_TO_BORDER` when the transform is rotated or skewed — draws a textured quad through the `Tessellator`, then disables blending and pops the matrix. You don't need to preconfigure blending.

When the transform is neither rotated nor skewed, the corners of the quad land on whole window pixels, like every rectangle (see [Pixel alignment](draw-utils.md#pixel-alignment)): an image drawn at `100.5` or under a fractional scale stays sharp instead of being resampled between two pixels, and keeps at least one pixel. The decoder gives every transparent pixel the color of its nearest visible pixel, so linear filtering never darkens the edges of a transparent image.

## See also

- `ResourceBuilder` — how to load and configure resources.
- `ResourceNode` — node-level wrapper with hover/click integration.
- `Decoders` — image and video decoders.