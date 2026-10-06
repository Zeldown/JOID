# Drawing Resources

`DrawResource` (`dev.joid.lib.draw.resource`) draws the texture of a `Resource` (an image, an SVG, the current frame of an animation or a video) immediately, through `DrawUtils.RESOURCE`: at its natural size, stretched to a box, or as a region of the source. It is what `ResourceNode` draws with; use it in your own draw hooks (see [Drawing Overview](draw-utils.md)).

## Quick example

```java
private final Resource icon = Resource.of(UIInventory.class.getResourceAsStream("/assets/ui/icon.png"));

@Override
public void postDraw(final double mouseX, final double mouseY) {
    if (this.icon.isLoaded()) {
        DrawUtils.RESOURCE.drawResource(40D, 40D, 64D, 64D, this.icon);
    }
}
```

`Resource` (`dev.joid.lib.resource`) loads asynchronously by default; how to create and configure one is on [Resources](../resources/resources.md).

## DrawResource reference

| Method | Description |
|---|---|
| `drawResource(double x, double y, Resource resource)` | Draws at the natural size: `resource.getWidth()` × `resource.getHeight()` UI units, or the size of its sprite region when it has one. |
| `drawResource(double x, double y, double width, double height, Resource resource)` | Stretches the texture, or its sprite region, over the box. |
| `drawResource(double x, double y, double width, double height, double u, double v, double regionWidth, double regionHeight, Resource resource)` | Stretches a region of the source over the box. |
| `DrawResource.getInstance()` | The instance behind `DrawUtils.RESOURCE`. |

## Drawing a region of the source

The region overload maps the rectangle `(u, v, regionWidth, regionHeight)` of the source onto the box. The region is in the units of `resource.getWidth()` and `resource.getHeight()` (the pixels of a bitmap image), from the top-left corner.

```java
final double scale = Math.max(width / resource.getWidth(), height / resource.getHeight());
final double regionWidth = width / scale;
final double regionHeight = height / scale;
DrawUtils.RESOURCE.drawResource(x, y, width, height, (resource.getWidth() - regionWidth) / 2D, (resource.getHeight() - regionHeight) / 2D, regionWidth, regionHeight, resource);
```

This crop fills the box without distortion and centers the image, like `ResourceNode` with `StretchType.COVER` (see [ResourceNode](../nodes/visual/resource.md)).

### Sprites with textureCoords

A resource can carry its own region with `textureCoords(double u, double v, double width, double height)` (on `Resource` or `ResourceBuilder`), in the same units. The first two overloads then draw that region: at its size for `drawResource(x, y, resource)`, stretched to the box for `drawResource(x, y, width, height, resource)`.

```java
final Resource sheet = Resource.of(UIGame.class.getResourceAsStream("/assets/ui/sheet.png"));
final Resource heart = sheet.copy().textureCoords(0D, 0D, 16D, 16D);
final Resource coin = sheet.copy().textureCoords(16D, 0D, 16D, 16D);

DrawUtils.RESOURCE.drawResource(20D, 20D, 32D, 32D, heart);
DrawUtils.RESOURCE.drawResource(60D, 20D, 32D, 32D, coin);
```

`copy()` shares the loaded data and gives each sprite its own properties.

## Tinting a resource

The quad is drawn with the current color of the render bridge, white by default, which multiplies the texture. Bind a `Color` around the call to tint the resource or fade it; pass `true` as the last argument of `bind` so a gradient color is applied over the texture:

```java
final Vector4f canvas = new Vector4f((float) x, (float) y, (float) (x + width), (float) (y + height));
Color.WHITE.copyAlpha(0.5F).bind(() -> DrawUtils.RESOURCE.drawResource(x, y, width, height, this.icon), canvas, true);
```

`canvas` (`javax.vecmath.Vector4f`, left, top, right, bottom) is the area a gradient spans. See [Colors and Gradients](../styling/colors.md).

## What a draw does

- **Before loading.** A resource that is not loaded yet has no texture, and the quad is drawn plain in the current color. Check `isLoaded()` first, or draw a placeholder as `ResourceNode` does.
- **Uploads and frames.** The call uploads the decoded data on first use and lets the decoder update the texture, which advances an animation or a video.
- **Pixel size request.** Without a sprite region, the call tells the decoder of the resource the size it is drawn at, in window pixels, so a vector image (SVG) is rendered at that size. The region overload asks for the size the whole source would have at the scale of the region.
- **Automatic mipmaps.** When the resource has no explicit `mipmap(...)` setting, uses linear interpolation, is mipmappable (`isMipmappable()`) and is drawn smaller than its texture, mipmaps are turned on for it, which keeps a downscaled image smooth. Call `mipmap(false)` to keep them off.
- **Pixel alignment.** The edges of the quad snap to the window pixels while the transform is axis-aligned, and never collapse below one pixel. A rotated or skewed image keeps its exact geometry (see [Pixel alignment](draw-utils.md#pixel-alignment)).
- **State.** The call pushes and pops the matrix, enables normal blending for its draw, then leaves blending disabled and no texture bound.

## See also

- [Resources](../resources/resources.md)
- [ResourceNode](../nodes/visual/resource.md)
- [Playback, Video and Audio](../resources/playback.md)
- [Drawing Overview](draw-utils.md)